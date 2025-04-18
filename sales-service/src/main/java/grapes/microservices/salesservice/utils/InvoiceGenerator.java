package grapes.microservices.salesservice.utils;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import grapes.microservices.salesservice.models.Article;
import grapes.microservices.salesservice.models.Order;
import grapes.microservices.salesservice.models.OrderItem;
import grapes.microservices.salesservice.repositories.ArticleRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class InvoiceGenerator {

    public static String generateInvoice(Order order, String customerName, String address, String postalCode, String country, String phoneNumber, List<OrderItem> items, ArticleRepository articleRepository) throws FileNotFoundException {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String filename = String.format("Invoice%03d_%s_%s.pdf", order.getId(), datePart, uuidPart);

        String folderPath = "uploads/invoices";
        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();

        String fullPath = folderPath + File.separator + filename;
        String publicUrl = "/invoices/" + filename;

        PdfWriter writer = new PdfWriter(fullPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        pdf.addEventHandler(com.itextpdf.kernel.events.PdfDocumentEvent.END_PAGE, new FooterHandler());

        // Logo
        try {
            InputStream logoStream = InvoiceGenerator.class.getClassLoader().getResourceAsStream("static/logo/grapes.png");
            if (logoStream != null) {
                byte[] logoBytes = logoStream.readAllBytes();
                ImageData imageData = ImageDataFactory.create(logoBytes);
                Image logo = new Image(imageData).scaleToFit(70, 70).setFixedPosition(470, 750);
                document.add(logo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        document.add(new Paragraph("GRAPES - INVOICE")
                .setFontSize(22)
                .setBold()
                .setFontColor(ColorConstants.MAGENTA)
                .setTextAlignment(TextAlignment.LEFT));

        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2})).useAllAvailableWidth();
        infoTable.addCell(createInfoCell("Order ID:"));
        infoTable.addCell(createValueCell(String.valueOf(order.getId())));
        infoTable.addCell(createInfoCell("User ID:"));
        infoTable.addCell(createValueCell(String.valueOf(order.getUserId())));
        infoTable.addCell(createInfoCell("Date:"));
        infoTable.addCell(createValueCell(LocalDate.now().toString()));
        document.add(infoTable);
        document.add(new Paragraph("\n"));

        Table customerTable = new Table(UnitValue.createPercentArray(new float[]{1, 2})).useAllAvailableWidth();
        customerTable.addCell(createInfoCell("Customer Name:"));
        customerTable.addCell(createValueCell(customerName));
        customerTable.addCell(createInfoCell("Address:"));
        customerTable.addCell(createValueCell(address));
        customerTable.addCell(createInfoCell("Postal Code:"));
        customerTable.addCell(createValueCell(postalCode));
        customerTable.addCell(createInfoCell("Country:"));
        customerTable.addCell(createValueCell(country));
        customerTable.addCell(createInfoCell("Phone:"));
        customerTable.addCell(createValueCell(phoneNumber));
        document.add(customerTable);
        document.add(new Paragraph("\n"));

        Table articleTable = new Table(UnitValue.createPercentArray(new float[]{1, 3, 1, 1, 1, 1}))
                .useAllAvailableWidth()
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1));

        String[] headers = {"Article ID", "Name", "Qty", "Qty (Kg)", "Price", "Total"};
        for (String h : headers) {
            articleTable.addHeaderCell(new Cell().add(new Paragraph(h).setBold())
                    .setBackgroundColor(ColorConstants.MAGENTA)
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        BigDecimal grandTotal = BigDecimal.ZERO;
        for (OrderItem item : items) {
            Article article = articleRepository.findById(item.getArticleId()).orElse(null);
            String name = article != null ? article.getName() : "Unknown";

            BigDecimal qty = (item.getQuantityKg() != null && item.getQuantityKg().compareTo(BigDecimal.ZERO) > 0)
                    ? item.getQuantityKg()
                    : item.getQuantity();
            BigDecimal total = item.getPrice().multiply(qty).setScale(2, RoundingMode.HALF_UP);
            grandTotal = grandTotal.add(total);

            articleTable.addCell(center(item.getArticleId()));
            articleTable.addCell(left(name));
            articleTable.addCell(center(item.getQuantity()));
            articleTable.addCell(center(item.getQuantityKg()));
            articleTable.addCell(right(item.getPrice()));
            articleTable.addCell(right(total));
        }

        document.add(articleTable);
        document.add(new Paragraph("\n"));

        Table totalTable = new Table(2).useAllAvailableWidth();
        totalTable.addCell(new Cell().add(new Paragraph("TOTAL"))
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER));
        totalTable.addCell(new Cell().add(new Paragraph(grandTotal.setScale(2, RoundingMode.HALF_UP) + " €"))
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER));
        document.add(totalTable);

        document.add(new Paragraph("\nThank you for shopping with GRAPES 🍇")
                .setItalic()
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));
        document.add(new Paragraph("Contact: contact@grapes-market.com | +32 2 123 45 67")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));

        document.close();

        String cleanedFilename = filename.trim().replaceAll(" ", "");

        publicUrl = publicUrl.trim().replaceAll(" ", "").replace("\\", "/");
        System.out.println(" Facture générée et accessible à : " + publicUrl);
        return publicUrl;

    }


    private static class FooterHandler implements com.itextpdf.kernel.events.IEventHandler {
        @Override
        public void handleEvent(com.itextpdf.kernel.events.Event event) {
            com.itextpdf.kernel.events.PdfDocumentEvent docEvent = (com.itextpdf.kernel.events.PdfDocumentEvent) event;
            PdfDocument pdf = docEvent.getDocument();
            int pageNum = pdf.getPageNumber(docEvent.getPage());
            int totalPages = pdf.getNumberOfPages();

            Paragraph footer = new Paragraph(String.format("Page %d of %d", pageNum, totalPages))
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER);

            float x = docEvent.getPage().getPageSize().getWidth() / 2;
            float y = 20;

            new com.itextpdf.layout.Canvas(
                    new com.itextpdf.kernel.pdf.canvas.PdfCanvas(docEvent.getPage()),
                    docEvent.getPage().getPageSize()
            ).showTextAligned(footer, x, y, TextAlignment.CENTER);
        }
    }



    // Utils
    private static Cell center(Object value) {
        return new Cell().add(new Paragraph(value != null ? value.toString() : "-"))
                .setTextAlignment(TextAlignment.CENTER);
    }

    private static Cell right(Object value) {
        return new Cell().add(new Paragraph(value != null ? value.toString() : "-"))
                .setTextAlignment(TextAlignment.RIGHT);
    }

    private static Cell left(Object value) {
        return new Cell().add(new Paragraph(value != null ? value.toString() : "-"))
                .setTextAlignment(TextAlignment.LEFT);
    }

    private static Cell createInfoCell(String text) {
        return new Cell().add(new Paragraph(text).setBold()).setBorder(Border.NO_BORDER);
    }

    private static Cell createValueCell(String text) {
        return new Cell().add(new Paragraph(text)).setBorder(Border.NO_BORDER);
    }
}
