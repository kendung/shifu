/*
 * Copyright [2013-2016] PayPal Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ml.shifu.shifu.core.eval;

import java.text.DecimalFormat;

/**
 * HTML gain chart generation template.
 * 
 * @author Zhang David (pengzhang@paypal.com)
 */
public class GainChartTemplate {

    public static String[] DEFAULT_COLORS = new String[] { "#7cb5ec", "#f15c80", "#2b908f", "#f7a35c", "#8085e9",
            "#e4d354", "#90ed7d", "#f45b5b", "#91e8e1" };

    public static String[] DEFAULT_LINE_STYLES = new String[] { "Solid", "Dash", "DashDot", "ShortDot", "ShortDash",
            "ShortDashDot", "ShortDashDotDot", "Dot", "LongDash", "LongDashDot", "LongDashDotDot" };

    public static String HIGHCHART_BASE_BEGIN = new StringBuilder(1000)
            .append("<!DOCTYPE html>")
            .append("\n")
            .append("<html>")
            .append("\n")
            .append("<head>")
            .append("\n")
            .append("  <script src=\"http://cdn.bootcss.com/jquery/2.1.1/jquery.min.js\"></script>")
            .append("\n")
            .append("  <script src=\"http://cdn.bootcss.com/bootstrap/3.2.0/js/bootstrap.min.js\"></script>")
            .append("\n")
            .append("  <link href=\"http://cdn.bootcss.com/bootstrap/3.2.0/css/bootstrap.min.css\" rel=\"stylesheet\" />")
            .append("\n").append("  <script src=\"http://cdn.bootcss.com/highcharts/4.0.4/highcharts.js\"></script>")
            .append("\n").append("  <style type=\"text/css\">").append("\n").append("    .sidebar {").append("\n")
            .append("      position: fixed;").append("\n").append("      top: 0px;").append("\n")
            .append("      bottom: 0px;").append("\n").append("      left: 0px;").append("\n")
            .append("      z-index: 1000;").append("\n").append("      display: block;").append("\n")
            .append("      padding: 10px;").append("\n").append("      overflow-x: hidden;").append("\n")
            .append("      overflow-y: auto;").append("\n").append("      background-color: #F5F5F5;").append("\n")
            .append("      border-right: 1px solid #EEE;").append("\n").append("    }").append("\n")
            .append("  </style>").append("\n").append("  <title>Advanced Gain Chart</title>").append("\n")
            .append("</head>").append("\n").append("<body>").append("\n").append("  <div class=\"container-fluid\">")
            .append("\n").append("    <div class=\"row\">").append("\n")
            .append("      <div class=\"col-sm-3 col-md-2 sidebar\" >").append("\n").toString();

    public static String HIGHCHART_SIDE_TEMPLATE = "%s\n%s\n";

    public static String HIGHCHART_BUTTON_PANEL_TEMPLATE_1 = new StringBuilder(200)
            .append("      <div class=\"panel panel-info\">").append("\n")
            .append("        <div class=\"panel-heading\">").append("\n")
            .append("          <h3 class=\"panel-title\">%s</h3>").append("\n").append("        </div>").append("\n")
            .append("        <div class=\"panel-body\">").append("\n")
            .append("           <a id=\"%s\" href=\"#\" class=\"list-group-item list-group-item-success\">%s</a>")
            .append("\n").append("           <a id=\"%s\" href=\"#\" class=\"list-group-item\">%s</a>").append("\n")
            .append("        </div>").append("\n").append("      </div>").append("\n").toString();

    public static String HIGHCHART_BUTTON_PANEL_TEMPLATE_2 = new StringBuilder(200)
            .append("      <div class=\"panel panel-info\">").append("\n")
            .append("        <div class=\"panel-heading\">").append("\n")
            .append("          <h3 class=\"panel-title\">%s</h3>").append("\n").append("        </div>").append("\n")
            .append("        <div class=\"panel-body\">").append("\n")
            .append("           <a id=\"%s\" href=\"#\" class=\"list-group-item\">%s</a>").append("\n")
            .append("           <a id=\"%s\" href=\"#\" class=\"list-group-item list-group-item-success\">%s</a>")
            .append("\n").append("        </div>").append("\n").append("      </div>").append("\n").toString();

    public static String HIGHCHART_BUTTON_PANEL_TEMPLATE_3 = new StringBuilder(200)
            .append("      <div class=\"panel panel-info\">").append("\n")
            .append("        <div class=\"panel-heading\">").append("\n")
            .append("          <h3 class=\"panel-title\">%s</h3>").append("\n").append("        </div>").append("\n")
            .append("        <div class=\"panel-body\">").append("\n")
            .append("           <a id=\"%s\" href=\"#\" class=\"list-group-item\">%s</a>").append("\n")
            .append("           <a id=\"%s\" href=\"#\" class=\"list-group-item\">%s</a>").append("\n")
            .append("        </div>").append("\n").append("      </div>").append("\n").toString();

    public static String HIGHCHART_LIST_PANEL_TEMPLATE = new StringBuilder(100)
            .append("      <div class=\"panel panel-info\">").append("        <div class=\"panel-heading\">")
            .append("          <h3 class=\"panel-title\">%s</h3>").append("        </div>")
            .append("        <ul class=\"list-group\">").append("          %s").append("        </ul>")
            .append("      </div>").toString();

    public static String HIGHCHART_WGT_LIST_TEMPLATE = "<a id=\"%s\" href=\"#\" class=\"list-group-item%s\">%s</a>";

    public static String HIGHCHART_BUTTON_TEMPLATE = "<button id=\"%s\" type=\"button\" class=\"btn btn-success\">%s</button>";

    public static String HIGHCHART_DIV = "          <div id=\"%s\" sytle=\"height: 400px; min-width: 600px\" class=\"show\"></div>\n";

    public static String HIGHCHART_DATA_TEMPLATE = "\n<script>\n%s\n";

    public static String HIGHCHART_FUNCTION_TEMPLATE = new StringBuilder(100).append("$(function () {").append("  %s")
            .append("});").append("").append("$(document).ready(function() {").append("  %s").append("});")
            .append("</script>").toString();

    public static String HIGHCHART_SERIES_TEMPLATE = new StringBuilder(100).append("      data: data_%d,")
            .append("      name: '%s',").append("      dashStyle: '%s',").append("      turboThreshold:0").toString();

    public static String HIGHCHART_TOOLTIP_TEMPLATE = "s += '%s: ' + this.point.%s + '%";

    public static String HIGHCHART_CHART_TEMPLATE = new StringBuilder(1000)
            .append("    $('#%s').highcharts({\n")
            .append("        chart: {\n")
            .append("            borderWidth: 1,\n")
            .append("            zoomType: 'x',\n")
            .append("            resetZoomButton: {\n")
            .append("                position: {\n")
            .append("                    align: 'left',\n")
            .append("                    x: 10,\n")
            .append("                    y: 10\n")
            .append("                },\n")
            .append("                relativeTo: 'chart'\n")
            .append("            },\n")
            .append("            marginRight: 80\n")
            .append("        },\n")
            .append("\n")
            .append("        title: {\n")
            .append("            text : '%s'\n")
            .append("        },\n")
            .append("        subtitle : {\n")
            .append("            text: '%s Model'\n")
            .append("        },\n")
            .append("\n")
            .append("        tooltip: {\n")
            .append("            shared: true,\n")
            .append("            useHTML: true,\n")
            .append("            headerFormat: '<table> <tr><th>line</th><th>&nbsp;Recall&nbsp;</th><th>&nbsp;Precision&nbsp;</th><th>&nbsp;$ opt&nbsp;</th><th>&nbsp;# opt&nbsp;</th><th>&nbsp;Score&nbsp;</th></tr>',\n")
            .append("            pointFormat: '<tr><td style=\"color:{series.color}\">{series.name}&nbsp;:</td><td>&nbsp;{point.y}%%&nbsp;</td><td>,&nbsp;{point.precision}%%&nbsp;</td>' + '<td>,&nbsp;{point.wgt_opt}%%&nbsp;</td>' + '<td>,&nbsp;{point.opt}%%&nbsp;</td>' + '<td>,&nbsp;{point.score}&nbsp;</td>' + '</tr>',\n")
            .append("            footerFormat: '</table>',\n").append("            crosshairs: true\n")
            .append("        },\n").append("\n").append("        yAxis: {\n").append("            title: {\n")
            .append("                text: 'Recall'\n").append("            },\n").append("            labels: {\n")
            .append("                formatter: function() {\n")
            .append("                    return this.value + '%%';\n").append("                }\n")
            .append("            },\n").append("            gridLineWidth: 1,\n")
            .append("            ceiling : 100,\n").append("            floor : 0\n").append("        },\n")
            .append("\n")
            .append("        xAxis: {\n")
            .append("            title: {\n")
            .append("                text: '%s'\n")
            .append("            },\n")
            .append("            labels: {\n")
            .append("                formatter: function() {\n")
            .append("                    return this.value + '%s';\n")
            .append("                }\n")
            .append("            },\n")
            .append("            reversed: %s,\n")
            .append("            gridLineWidth: 1\n")
            .append("        },\n").append("\n")
            .append("        credits : {\n")
            .append("            enabled: false\n")
            .append("        },\n").append("\n")
            .append("        legend: {\n").append("            align : 'right',\n")
            .append("            verticalAlign: 'middle',\n").append("            layout : 'vertical',\n")
            .append("            borderWidth : 1,\n").append("            floating: true,\n")
            .append("            backgroundColor: 'white'\n").append("        },\n").append("\n")
            .append("        series: [{\n").append("            data: %s,\n")
            .append("            name: '%s, %s score',\n").append("            turboThreshold:0\n")
            .append("        }]\n").append("    });").toString();

    public static String DATA_FORMAT = "{y: %s, x: %s, wgt_opt: %s, precision: %s, opt: %s, score: %s}";

    public static String HIGHCHART_GROUP_TOGGLE_TEMPLATE = new StringBuilder(200).append("$('#%s').click(function() {")
            .append("\n").append("  var cs = ['%s'];").append("\n").append("  var cl = cs.length;").append("\n")
            .append("  for (var i = 0; i < cl; i++) {").append("\n")
            .append("    if (! ($(cs[i]).hasClass('ls_chosen') || $(cs[i]).hasClass('bd_chosen'))) {").append("\n")
            .append("      $(cs[i]).toggleClass('show');").append("\n").append("      $(cs[i]).toggleClass('hidden');")
            .append("\n").append("    }").append("\n").append("    $(cs[i]).toggleClass('gp_chosen');").append("\n")
            .append("  };").append("\n").append("  $('%s').toggleClass('list-group-item-success');").append("\n")
            .append("});").toString();

    public static String HIGHCHART_BAD_TOGGLE_TEMPLATE = new StringBuilder(200).append("$('#%1$s').click(function() {")
            .append("  var cs = [%2$s];").append("  var cl = cs.length;").append("  for (var i = 0; i < cl; i++) {")
            .append("    if (! ($(cs[i]).hasClass('ls_chosen') || $(cs[i]).hasClass('gp_chosen'))) {")
            .append("      $(cs[i]).toggleClass('show');").append("      $(cs[i]).toggleClass('hidden');")
            .append("    }").append("    $(cs[i]).toggleClass('bd_chosen');").append("  };")
            .append("  $('#%1$s').toggleClass('btn-success');").append("  $('#%1$s').toggleClass('btn-default');")
            .append("});").toString();

    public static String HIGHCHART_LIST_TOGGLE_TEMPLATE = new StringBuilder(200)
            .append("")
            .append("$('#%s').click(function() {")
            .append("\n")
            .append("  var cs = ['#%s'];")
            .append("\n")
            .append("  var cl = cs.length;")
            .append("\n")
            .append("  for (var i = 0; i < cl; i++) {")
            .append("\n")
            .append("    if (! ($(cs[i]).hasClass('gp_chosen') || $(cs[i]).hasClass('bd_chosen') || $(cs[i]).hasClass('ft_chosen'))) {")
            .append("\n").append("      $(cs[i]).toggleClass('show');").append("\n")
            .append("      $(cs[i]).toggleClass('hidden');").append("\n").append("    }").append("\n")
            .append("    $(cs[i]).toggleClass('ls_chosen');").append("\n").append("  };").append("\n")
            .append("  $('#%s').toggleClass('list-group-item-success');").append("\n").append("});").toString();

    public static String HIGHCHART_READY_TOGGLE_TEMPLATE = new StringBuilder(1000).append("")
            .append("  var ics = [%s];").append("  var icl = ics.length;").append("  for (var i = 0; i < icl; i++) {")
            .append("    $(ics[i]).toggleClass('show');").append("    $(ics[i]).toggleClass('hidden');")
            .append("    $(ics[i]).toggleClass('ls_chosen');").append("  };").toString();

    public static String HIGHCHART_BASE_END = "    </div>\n" + "  </div>\n" + "</body>\n";

    public static String HIGHCHART_TIPTABLE_HEAD = "<tr><th>line</th><th>&nbsp;catch&nbsp;</th><th>&nbsp;fpr&nbsp;</th>";

    public static String HIGHCHART_TIPTABLE_STRING = "'<tr><td style=\"color:{series.color}\">{series.name}&nbsp;:</td><td>&nbsp;{point.y}%&nbsp;</td><td>,&nbsp;{point.fpr}&nbsp;</td>'";

    public static final DecimalFormat DF = new DecimalFormat("#.##");

}
