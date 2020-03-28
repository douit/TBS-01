package sa.tamkeentech.tbs.service.util;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * Created by Ahmed B on 15/01/2020.
 */
@Slf4j
public class JasperReportExporter<E> {

	/**
	 * PDF mime type
	 */
	public static final String CONTENT_TYPE = "application/pdf";

	/**
	 * Cache
	 */
	private Map<String, JasperReport> templatesCache;

	private static class SingletonHolder {
		// Instance .
		public static final JasperReportExporter<Object> INSTANCE = new JasperReportExporter<Object>();
	}

	private JasperReportExporter() {
	}

	public static JasperReportExporter<?> getInstance() {
		if (SingletonHolder.INSTANCE.templatesCache == null) {
			SingletonHolder.INSTANCE.templatesCache = new HashMap<String, JasperReport>();
		}
		return SingletonHolder.INSTANCE;
	}



	public String exportPDF(HttpServletRequest request, List<?> dataBean, Map<String, Object> parameters, String jrxmltemplateName, String reportName) {
		String generatedReportPath = null;
		try {
			ServletContext context = request.getSession().getServletContext();
			JasperDesign jasperDesign;

			jasperDesign = JRXmlLoader.load(context.getRealPath(jrxmltemplateName));

			JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, setDataSource(dataBean));
			String reportPath = context.getRealPath("/report/") + "/" + reportName;
			JasperExportManager.exportReportToPdfFile(jasperPrint, reportPath);
			generatedReportPath = context.getContextPath() + "/report/" + reportName;
		} catch (JRException e) {
			log.error("ExportPDF Exception: {}", e);
		}
		return generatedReportPath;

	}

    public String exportExcel(HttpServletRequest request, List<?> dataBean, Map<String, Object> parameters, String jrxmltemplateName, String reportName) {
		String generatedReportPath = null;
		try {
			ServletContext context = request.getSession().getServletContext();
			JasperDesign jasperDesign;

			jasperDesign = JRXmlLoader.load(context.getRealPath(jrxmltemplateName));

			JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, setDataSource(dataBean));
			String reportPath = context.getRealPath("/report/") + "/" + reportName;

			JRXlsExporter exporter = new JRXlsExporter();
			exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(reportPath));
			SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
			configuration.setOnePagePerSheet(true);
			configuration.setDetectCellType(true);
			configuration.setCollapseRowSpan(false);
			exporter.setConfiguration(configuration);

			exporter.exportReport();
			generatedReportPath = context.getContextPath() + "/report/" + reportName;
		} catch (JRException e) {
			log.error("ExportExcel Exception: {}", e);
		}
		return generatedReportPath;

	}

	public byte[] generatePdfReport(List<?> dataBean, Map<String, Object> parameters, String jrxmltemplateName, boolean isOnePage) throws JRException {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("report/");
			builder.append(jrxmltemplateName);
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(builder.toString());
			JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, setDataSource(dataBean));
			removeBlankPage(jasperPrint.getPages(), isOnePage);
			ByteArrayOutputStream report = new ByteArrayOutputStream();
			if (jasperPrint != null) {
				return JasperExportManager.exportReportToPdf(jasperPrint);
			}
			return report.toByteArray();

		} catch (JRException e) {
			e.printStackTrace();
			throw new JRException(e.getMessage());
		}

	}

	public byte[] generateXlsReport(List<?> dataBean, Map<String, Object> parameters, String jrxmltemplateName) throws JRException {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("report/");
			builder.append(jrxmltemplateName);
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(builder.toString());
			JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, setDataSource(dataBean));
			removeBlankPage(jasperPrint.getPages());
			ByteArrayOutputStream report = new ByteArrayOutputStream();
			JRXlsxExporter exporter = new JRXlsxExporter();
			if (jasperPrint != null) {
				exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
				exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(report));
				SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
				//avoid multi pages
				//configuration.setOnePagePerSheet(true);
				configuration.setDetectCellType(true);
				configuration.setCollapseRowSpan(false);
				exporter.setConfiguration(configuration);
				exporter.exportReport();
			}
			return report.toByteArray();

		} catch (JRException e) {
			e.printStackTrace();
			throw new JRException(e.getMessage());
		}

	}

	public byte[] generateMultiSheetsXlsReport(List<List<?>> dataBeans, Map<String, Object> parameters, String jrxmltemplateName, List<String> sheetNames) throws JRException {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("report/");
			builder.append(jrxmltemplateName);
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(builder.toString());
			JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

			//JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, setDataSource(dataBean));
			//removeBlankPage(jasperPrint.getPages());
			List<JasperPrint> sheets = new ArrayList<JasperPrint>();
			for (int i=0;i<dataBeans.size();i++){
				JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, setDataSource(dataBeans.get(i)));
				sheets.add(print);
			}
			ByteArrayOutputStream report = new ByteArrayOutputStream();
			JRXlsxExporter exporter = new JRXlsxExporter();
			if (sheets.size() >= 0) {
				// exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
				exporter.setExporterInput(SimpleExporterInput.getInstance(sheets));
				exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(report));
				SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
				configuration.setSheetNames(sheetNames.toArray(new String[sheetNames.size()]));
				//avoid multi pages
				//configuration.setOnePagePerSheet(true);
				configuration.setDetectCellType(true);
				configuration.setCollapseRowSpan(false);
				exporter.setConfiguration(configuration);
				exporter.exportReport();
			}
			return report.toByteArray();

		} catch (JRException e) {
			e.printStackTrace();
			throw new JRException(e.getMessage());
		}
	}

	/**
	 * Remove blank pages
	 *
	 * @param pages
	 */
	private void removeBlankPage(List<JRPrintPage> pages) {

		if(pages != null) {
				for (Iterator<JRPrintPage> i = pages.iterator(); i.hasNext();) {
					JRPrintPage page = i.next();
					if (page.getElements().size() == 0)
						i.remove();
				}
			}

	  }


	private void removeBlankPage(List<JRPrintPage> pages, boolean isOnePage) {

		if(pages != null) {
			boolean isFirst = true;
			for (Iterator<JRPrintPage> i = pages.iterator(); i.hasNext();) {
				JRPrintPage page = i.next();
				if (page.getElements().size() == 0 || (isOnePage && !isFirst))
					i.remove();

				isFirst = false;
			}
		}

	  }


	/**
	 * @param listDto
	 *            bean dto
	 *
	 * @return JRDataSource
	 */
	private JRDataSource setDataSource(Collection<?> listDto) {
		if (listDto.size() != 0) {
			return new JRBeanCollectionDataSource(listDto, false);
		} else {
			return new JREmptyDataSource();
		}
	}

	protected void setContentType(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType(CONTENT_TYPE);
	}

	protected void setContentDisposition(HttpServletResponse response, String fileName) {
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
	}
}
