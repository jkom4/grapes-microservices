package grapes.microservices.salesservice.utils;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import grapes.microservices.salesservice.models.Article;
import grapes.microservices.salesservice.models.Order;
import grapes.microservices.salesservice.models.OrderItem;
import grapes.microservices.salesservice.repositories.ArticleRepository;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class InvoiceGenerator {
    /**
     * Generates a PDF invoice for a given order and its associated order items.
     * <p>
     * The invoice includes:
     * <ul>
     *     <li>Order ID and User ID</li>
     *     <li>A table listing each article: ID, name, quantity, quantity in kilograms, unit price, and total</li>
     *     <li>The grand total of the order</li>
     * </ul>
     * The PDF file is saved to the "invoices" directory and given a unique name based on the order ID,
     * the current date, and a UUID.
     *
     * @param order             the {@link Order} object containing order details
     * @param items             the list of {@link OrderItem} in the order
     * @param articleRepository the repository used to fetch article names by ID
     * @return the file path to the generated PDF invoice
     * @throws FileNotFoundException if the PDF file cannot be created
     */
    public static String generateInvoice(Order order, List<OrderItem> items, ArticleRepository articleRepository) throws FileNotFoundException {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // ex: 20250401
        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String filename = String.format("Invoice%03d_%s_%s.pdf", order.getId(), datePart, uuidPart);
        String folderPath = "invoices";

        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();

        String fullPath = folderPath + File.separator + filename;

        PdfWriter writer = new PdfWriter(fullPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("INVOICE").setBold().setFontSize(20));
        document.add(new Paragraph("Order ID: " + order.getId()));
        document.add(new Paragraph("User ID: " + order.getUserId()));
        document.add(new Paragraph("--------------------------------------------"));

        // ✅ 6 columns : ID, Nom, Qty, QtyKg, Prix, Total
        Table table = new Table(6);
        table.addHeaderCell("Article ID");
        table.addHeaderCell("Name");
        table.addHeaderCell("Qty");
        table.addHeaderCell("Qty (Kg)");
        table.addHeaderCell("Price");
        table.addHeaderCell("Total");

        BigDecimal grandTotal = BigDecimal.ZERO;

        for (OrderItem item : items) {
            BigDecimal qty = item.getQuantityKg() != null ? item.getQuantityKg() : item.getQuantity();
            BigDecimal total = item.getPrice().multiply(qty).setScale(2, RoundingMode.HALF_UP);
            grandTotal = grandTotal.add(total);

            Article article = articleRepository.findById(item.getArticleId()).orElse(null);
            String name = article != null ? article.getName() : "Unknown";

            table.addCell(new Cell().add(new Paragraph(item.getArticleId().toString())));
            table.addCell(new Cell().add(new Paragraph(name)));
            table.addCell(new Cell().add(new Paragraph(item.getQuantity() != null ? item.getQuantity().toString() : "-")));
            table.addCell(new Cell().add(new Paragraph(item.getQuantityKg() != null ? item.getQuantityKg().toString() : "-")));
            table.addCell(new Cell().add(new Paragraph(item.getPrice().setScale(2, RoundingMode.HALF_UP).toString())));
            table.addCell(new Cell().add(new Paragraph(total.toString())));
        }

        document.add(table);
        document.add(new Paragraph("\nTOTAL: " + grandTotal.setScale(2, RoundingMode.HALF_UP) + " €").setBold());
        document.close();

        return fullPath;
    }
}
