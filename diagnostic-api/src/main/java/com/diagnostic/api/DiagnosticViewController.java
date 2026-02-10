package com.diagnostic.api;

import com.diagnostic.core.model.HealthReport;
import com.diagnostic.report.HtmlReportExporter;
import com.diagnostic.report.ReportGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Web controller for viewing diagnostic reports in browser.
 */
@Controller
@RequestMapping("/diagnostic")
public class DiagnosticViewController {

    private final ReportGenerator reportGenerator;
    private final HtmlReportExporter htmlExporter;

    public DiagnosticViewController(ReportGenerator reportGenerator,
                                   HtmlReportExporter htmlExporter) {
        this.reportGenerator = reportGenerator;
        this.htmlExporter = htmlExporter;
    }

    /**
     * View health report in browser.
     * Access at: http://localhost:8080/diagnostic/report
     */
    @GetMapping("/report")
    @ResponseBody
    public String viewReport() {
        HealthReport report = reportGenerator.generateReport();
        return htmlExporter.exportToHtml(report);
    }

    /**
     * Dashboard page with links to all diagnostic features.
     * Access at: http://localhost:8080/diagnostic/dashboard
     */
    @GetMapping("/dashboard")
    @ResponseBody
    public String dashboard() {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <title>诊断工具仪表板</title>" +
                "    <style>" +
                "        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; " +
                "               background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
                "               margin: 0; padding: 40px; min-height: 100vh; }" +
                "        .container { max-width: 800px; margin: 0 auto; }" +
                "        h1 { color: white; text-align: center; font-size: 3em; margin-bottom: 10px; }" +
                "        .subtitle { color: rgba(255,255,255,0.9); text-align: center; margin-bottom: 40px; }" +
                "        .card { background: white; border-radius: 12px; padding: 30px; " +
                "                box-shadow: 0 10px 30px rgba(0,0,0,0.2); margin-bottom: 20px; }" +
                "        .card h2 { color: #667eea; margin-top: 0; }" +
                "        .links { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; }" +
                "        .link-btn { display: block; padding: 15px 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
                "                    color: white; text-decoration: none; border-radius: 8px; text-align: center; " +
                "                    transition: transform 0.2s, box-shadow 0.2s; }" +
                "        .link-btn:hover { transform: translateY(-2px); box-shadow: 0 5px 15px rgba(102,126,234,0.4); }" +
                "        .api-link { font-family: monospace; font-size: 0.9em; color: #667eea; " +
                "                    display: block; margin: 5px 0; text-decoration: none; }" +
                "        .api-link:hover { text-decoration: underline; }" +
                "        .method { display: inline-block; padding: 2px 8px; border-radius: 4px; " +
                "                  font-size: 0.8em; font-weight: bold; margin-right: 8px; }" +
                "        .get { background: #61affe; color: white; }" +
                "        .post { background: #49cc90; color: white; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <h1>🏥 诊断工具仪表板</h1>" +
                "        <p class='subtitle'>Spring Boot 应用健康诊断与架构洞察</p>" +
                "        " +
                "        <div class='card'>" +
                "            <h2>📊 可视化报告</h2>" +
                "            <div class='links'>" +
                "                <a href='/diagnostic/report' class='link-btn' target='_blank'>查看健康报告</a>" +
                "            </div>" +
                "        </div>" +
                "        " +
                "        <div class='card'>" +
                "            <h2>🔌 REST API 端点</h2>" +
                "            <p><strong>健康报告</strong></p>" +
                "            <a href='/diagnostic/health' class='api-link' target='_blank'>" +
                "                <span class='method get'>GET</span>/diagnostic/health - 完整健康报告</a>" +
                "            <a href='/diagnostic/health/summary' class='api-link' target='_blank'>" +
                "                <span class='method get'>GET</span>/diagnostic/health/summary - 报告摘要</a>" +
                "            " +
                "            <p><strong>依赖拓扑</strong></p>" +
                "            <a href='/diagnostic/topology' class='api-link' target='_blank'>" +
                "                <span class='method get'>GET</span>/diagnostic/topology - 依赖拓扑</a>" +
                "            <a href='/diagnostic/topology/json' class='api-link' target='_blank'>" +
                "                <span class='method get'>GET</span>/diagnostic/topology/json - 拓扑JSON</a>" +
                "            <a href='/diagnostic/topology/dot' class='api-link' target='_blank'>" +
                "                <span class='method get'>GET</span>/diagnostic/topology/dot - DOT格式</a>" +
                "            " +
                "            <p><strong>端点统计</strong></p>" +
                "            <a href='/diagnostic/endpoints' class='api-link' target='_blank'>" +
                "                <span class='method get'>GET</span>/diagnostic/endpoints - 所有端点统计</a>" +
                "            " +
                "            <p><strong>架构风险</strong></p>" +
                "            <a href='/diagnostic/risks' class='api-link' target='_blank'>" +
                "                <span class='method get'>GET</span>/diagnostic/risks - 所有风险</a>" +
                "            <a href='/diagnostic/risks/high' class='api-link' target='_blank'>" +
                "                <span class='method get'>GET</span>/diagnostic/risks/high - 高严重性风险</a>" +
                "            " +
                "            <p><strong>系统状态</strong></p>" +
                "            <a href='/diagnostic/status' class='api-link' target='_blank'>" +
                "                <span class='method get'>GET</span>/diagnostic/status - 健康检查</a>" +
                "        </div>" +
                "        " +
                "        <div class='card'>" +
                "            <h2>🧪 测试端点</h2>" +
                "            <p>访问这些端点来生成测试数据：</p>" +
                "            <a href='/api/users' class='api-link' target='_blank'>" +
                "                <span class='method get'>GET</span>/api/users - 获取所有用户</a>" +
                "            <a href='/api/users/slow' class='api-link' target='_blank'>" +
                "                <span class='method get'>GET</span>/api/users/slow - 慢接口测试</a>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}
