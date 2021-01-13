package ml.shifu.shifu.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

/**
 * {@link GSUtils} is a utility class to operate google storage bucket. 
 * This class relies on gsutil command line tool to copy files from local file system to google storage bucket.
 * Make sure gsutil environment is set up before using this utility.
 * Example for gsutil command (copy all the files within local DataSet1 directory to remote bucket)
 * cd ${shifu_home}/example/cancer-judgement/DataStore/DataSet1
 * gsutil cp -r ./DataSet1 gs://${existing_bucket_name}/user/xuedeng/cancer-judgement/DataSet1
 */
public class GSUtils {

    private GSUtils(){
         // prevent new GSUtils();
    }

    /**
     * Copy file from local directory to destination directory in the gcp bucket
     * @param srcDirectory
     * @param bucket_name
     * @param destDirectory
     * @throws Exception
     */
    public static void copyFromLocalFile(String srcDirectory, String bucket_name, String destDirectory) throws Exception{
        File srcDirectoryFile =new File(srcDirectory);
        if (!srcDirectoryFile.exists()){
            String msg = MessageFormat.format("Directory -{0} does not exist.", srcDirectory);
            throw new Exception(msg);
        }
        if (StringUtils.isBlank(bucket_name)){
            throw new Exception("Please specify GCP bucket name via environment variable GS_BUCKET.");
        }
        if (bucketExists(bucket_name)){
            if (!StringUtils.startsWith(destDirectory, "/")) {
                destDirectory = "/" + destDirectory;
            }
            String gsutilCmd = MessageFormat.format("gsutil cp -r {0} gs://{1}{2}", srcDirectory, bucket_name, destDirectory );
            CommandExecutionOutput output = executeCommand(gsutilCmd);
            if (output != null && output.code != 0){
                throw new Exception(output.details.stream().collect(Collectors.joining("\n")));
            }
        }
    }

    /**
     * Check whether bucket exists or not
     * @param bucket_name
     * @return
     * @throws Exception
     */
    public static boolean bucketExists(String bucket_name) throws Exception{
        String gsutilCmd = MessageFormat.format("gsutil ls gs://{0}", bucket_name);
        CommandExecutionOutput output = executeCommand(gsutilCmd);
        if (output != null && output.code != 0){
            throw new Exception(output.details.stream().collect(Collectors.joining("\n")));
        }
        boolean bucketExists = true;
        for(String msg : output.details){
            if (msg.startsWith("BucketNotFoundException")){
                bucketExists = false;
                break;
            }
        }
        if (!bucketExists){
            throw new Exception(MessageFormat.format("Bucket -{0} does not exist.", bucket_name));
        }
        return true;
    }

    /**
     * check whether gsutil command is available locally
     * @return
     * @throws Exception
     */
    public static boolean gsutilCommandExists() throws Exception{
        String cmd = "gsutil";
        CommandExecutionOutput output = executeCommand(cmd);
        return output != null && output.code == 0;
    }

    /**
     * check whether environment is google cloud env or not
     * @return
     * @throws Exception
     */
    public static boolean isGoogleCloudEnvironment() throws Exception{
        String cmd = "gcloud config list";
        CommandExecutionOutput output = executeCommand(cmd);
        return output != null && output.code == 0;
    }

    /**
     * Execute shell command
     * @param command
     * @return CommandExecutionOutput
     * @throws Exception
     */
    public static CommandExecutionOutput executeCommand(String command) throws Exception{
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", command);
        Process process = builder.start();
        final AtomicReference<List<String>> info = new AtomicReference<List<String>>(new ArrayList<String>());
        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), new Consumer<String>(){
            public void accept(String line){
                info.get().add(line);
            }
        });
        StreamGobbler streamGobblerForError = new StreamGobbler(process.getErrorStream(), new Consumer<String>(){
            public void accept(String error){
                info.get().add(error);
            }
        });
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        Executors.newSingleThreadExecutor().submit(streamGobblerForError);
        int exitCode = process.waitFor();
        return new CommandExecutionOutput(exitCode, info.get());
    }

    /**
     * Execute command vi shell, and return input stream based on command output stream
     * @param command
     * @return
     * @throws IOException
     */
    private static InputStream getInputStreamByCmdOutput(String command) throws IOException{
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", command);
        Process process = builder.start();
        return process.getInputStream();
    }

    /**
     * Return fully qualified path
     * @param bucket_name
     * @param filePath
     * @return
     */
    public static String getFullyQualifiedPath(String bucket_name, String filePath){
        if (!filePath.startsWith("gs://")){
            filePath = MessageFormat.format("gs://{0}{1}", bucket_name, filePath);
        }
        return filePath;
    }
    
    /**
     * Return file input stream for file on GCP storage bucket
     * @param bucket_name
     * @param filePath
     * @return
     * @throws IOException
     */
    public static InputStream getFileInputStream(String bucket_name, String filePath) throws IOException{
        filePath = GSUtils.getFullyQualifiedPath(bucket_name, filePath);
        String cmd =  MessageFormat.format("gsutil cat {0}", filePath);
        return GSUtils.getInputStreamByCmdOutput(cmd);
    }

    /**
     * Check whether path exists or not in gcp storage bucket
     * @param bucket_name
     * @param filePath
     * @return
     * @throws IOException
     */
    public static boolean isFileExists(String bucket_name, String filePath) throws IOException{
        try{
            filePath = GSUtils.getFullyQualifiedPath(bucket_name, filePath);
            String cmd =  MessageFormat.format("gsutil ls {0}", filePath);
            CommandExecutionOutput output = GSUtils.executeCommand(cmd);
            if (output.code != 0){
                throw new IOException(output.details.stream().collect(Collectors.joining("\n")));
            }
            return true;
        }catch(Exception ex){
            throw new IOException(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * 
     * @param bucket_name
     * @param path
     * @return
     * @throws IOException
     */
    public static GSFileIterator listFiles(String bucket_name, String path) throws IOException{
        try{
            if (!path.endsWith("/")){
                path = path + "/";
            }
            String fullQualifiedPath = GSUtils.getFullyQualifiedPath(bucket_name, path);
            String cmd = MessageFormat.format("gsutil du {0}", fullQualifiedPath);
            CommandExecutionOutput output = GSUtils.executeCommand(cmd);
            List<GSFileItem> items = new ArrayList<GSFileItem>();
            GSFileIterator iter = new GSFileIterator(items);
            if (output.code == 0){
                List<String> entries = output.details;
                for(String entry : entries){
                    String[] fileInfo = StringUtils.split(entry, " ");
                    if (fileInfo.length != 2){
                        continue;
                    }
                    if (fileInfo[1].endsWith("/")){
                        continue;
                    }
                    String fileName = StringUtils.removeStart(fileInfo[1], fullQualifiedPath);
                    if (!StringUtils.contains(fileName, "/")){
                        items.add(new GSFileItem(fileInfo[1], Long.parseLong(fileInfo[0]), fileName));
                    }
                }
            }
            return iter;
        }catch(Exception ex){
            throw new IOException(ex.getLocalizedMessage(), ex);
        }
        
    }

    /**
     * CommandExecutionOutput is the holder of command execution result
     * code represents the exit code of the command process
     * details represents the command execution details
     */
    private static class CommandExecutionOutput{
        private int code;
        private List<String> details;
        public CommandExecutionOutput(int code, List<String> details){
            this.code = code;
            this.details = details;
        }
    }
    /**
     * StreamGobbler is used to consumer the output from process
     */
    private static class StreamGobbler implements Runnable{
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer){
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run(){
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
    }

    public static class GSFileIterator implements Iterator<GSFileItem>{
        private List<GSFileItem> children;
        private int index = 0;
        public GSFileIterator(List<GSFileItem> children){
            this.children = children;
            this.index = 0;
        }
        @Override
        public boolean hasNext(){
            return index <= (this.children.size()-1);
        }

        @Override
        public GSFileItem next(){
            return children.get(index++);
        }
    }

    public static class GSFileItem{
        private String path;
        private long size;
        private String fileName;

        public GSFileItem(String path, long size, String fileName){
            this.path = path;
            this.size = size;
            this.fileName = fileName;
        }

        public String getPath(){
            return this.path;
        }

        public long getSize(){
            return this.size;
        }

        public String getName(){
            return this.fileName;
        }
    }

}
