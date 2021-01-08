package ml.shifu.shifu.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
        if (bucketExists(bucket_name)){
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

}
