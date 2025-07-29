package org.mustangproject.ZUGFeRD;

import static java.util.Objects.isNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.mustangproject.CalculatedInvoice;

public class TaxCategoryCodeTest extends ResourceCase {
    
    /**
     * import factur-x test file and check zf items
     */
    @Test
    public void testImport() {
        final String facturxFileName = "multi_taxcode_test-factur-x-generated.xml";
        final File facturxInputFile = getResourceAsFile(String.format("taxcode/%s", facturxFileName));
        try {
            final ZUGFeRDInvoiceImporter zii = new ZUGFeRDInvoiceImporter(new FileInputStream(facturxInputFile));
            final CalculatedInvoice i = new CalculatedInvoice();
            zii.extractInto(i);
            checkItems(i.getZFItems());
        } catch (IOException e) {
            fail("IOException not expected");
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * import factur-x test file and 
     */
    @Test
    public void testExport() {
        final String facturxFileName = "multi_taxcode_test-factur-x-generated.xml";
        final String invoiceFileName = "multi-tax-code-pdf.pdf";
        final String zugferdFileName = "multi-tax-code-zugferd.pdf";
        final String testResourcePath = String.format("%s%s",this.getClass().getResource("/").getPath(), "taxcode");
        final File facturXInputFile = getResourceAsFile(String.format("taxcode/%s", facturxFileName));
        
        try (final ZUGFeRDExporterFromA3 zugferdExporter = new ZUGFeRDExporterFromA3()) {
            final ZUGFeRDInvoiceImporter zii = new ZUGFeRDInvoiceImporter(new FileInputStream(facturXInputFile));
            final CalculatedInvoice ci = new CalculatedInvoice();
            zii.extractInto(ci);   

            final String invoicePdfFile = String.format("%s/%s",testResourcePath,invoiceFileName);
            //final Path inputFilePath = Paths.get();
            zugferdExporter.setProfile(Profiles.getByName("EXTENDED", 2));
            zugferdExporter.load(invoicePdfFile);
            zugferdExporter.setProducer("UNIT TEST").setCreator("UNIT TEST");
            zugferdExporter.setTransaction(ci);

            Path outputFilePath = Paths.get(testResourcePath, zugferdFileName);
            Files.deleteIfExists(outputFilePath);
            zugferdExporter.export(outputFilePath.toString());            
        } catch (IOException e) {
            fail("IOException not expected");
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        try {
            final File zugferdInvoiceFile = new File(String.format("%s/%s", testResourcePath, zugferdFileName));
            final ZUGFeRDInvoiceImporter zii = new ZUGFeRDInvoiceImporter(new FileInputStream(zugferdInvoiceFile));
            final CalculatedInvoice ci = new CalculatedInvoice();
            zii.extractInto(ci);
            checkItems(ci.getZFItems());
        } catch (IOException e) {
            fail("IOException not expected");
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * check tax codes (seller assigned provides hint to taxcode)
     * @param items
     */
    private void checkItems(final IZUGFeRDExportableItem[] items) {
        for (final IZUGFeRDExportableItem item : items) {
            final IZUGFeRDExportableProduct product = item.getProduct();
            switch (product.getSellerAssignedID()) {
                case "TC-AE":
                    checkTaxCodeAE(product);
                    break;
                case "TC-Z":
                    checkTaxCodeZ(product);
                    break;
                case "TC-O":
                    checkTaxCodeO(product);
                    break;
                case "TC-K":
                    checkTaxCodeK(product);
                    break;
                case "TC-S":
                    checkTaxCodeS(product);
                    break;
                default:
                    fail(String.format("Unchecked: [%s] %s - %s (perc=%s rev=%s inter=%s)",
                            product.getSellerAssignedID(), product.getTaxCategoryCode(),
                            product.getTaxExemptionReason(), product.getVATPercent(), product.isReverseCharge(),
                            product.isIntraCommunitySupply()));
            }
        }
    }

    /**
     * VAT Percent must be 0%
     * Must have exemption reason
     * Reverse must be true
     * Intra community must be true
     * 
     * @param product
     */
    private void checkTaxCodeAE(final IZUGFeRDExportableProduct product) {
        final String expectedCode = "AE";
        final BigDecimal expectedPercent = new BigDecimal(0.00);
        final boolean expectedIsReverse = true;
        final boolean expectedIsIntraCommunitySupply = false;
        final boolean needsExemptionReason = true;

        System.out.println(String.format("[%s] %s - %s (perc=%s rev=%s inter=%s)", product.getSellerAssignedID(),
                product.getTaxCategoryCode(), product.getTaxExemptionReason(), product.getVATPercent(),
                product.isReverseCharge(), product.isIntraCommunitySupply()));
        assertEquals("Code", product.getTaxCategoryCode(), expectedCode);
        assertEquals("Percent", product.getVATPercent().compareTo(expectedPercent), 0);
        assertEquals("Reverse", product.isReverseCharge(), expectedIsReverse);
        assertEquals("Intra", product.isIntraCommunitySupply(), expectedIsIntraCommunitySupply);
        assertEquals("Reason", stringNotNullOrEmpty(product.getTaxExemptionReason()), needsExemptionReason);
        return;
    }

    /**
     * VAT Percent must be 0%
     * Must have exemption reason
     * Reverse must be false
     * Intra community must be true
     * 
     * @param product
     */
    private void checkTaxCodeZ(final IZUGFeRDExportableProduct product) {
        final String expectedCode = "Z";
        final BigDecimal expectedPercent = new BigDecimal(0.00);
        final boolean expectedIsReverse = false;
        final boolean expectedIsIntraCommunitySupply = false;
        final boolean needsExemptionReason = true;

        System.out.println(String.format("[%s] %s - %s (perc=%s rev=%s inter=%s)", product.getSellerAssignedID(),
                product.getTaxCategoryCode(), product.getTaxExemptionReason(), product.getVATPercent(),
                product.isReverseCharge(), product.isIntraCommunitySupply()));
        assertEquals("Code", product.getTaxCategoryCode(), expectedCode);
        assertEquals("Percent", product.getVATPercent().compareTo(expectedPercent), 0);
        assertEquals("Reverse", product.isReverseCharge(), expectedIsReverse);
        assertEquals("Intra", product.isIntraCommunitySupply(), expectedIsIntraCommunitySupply);
        assertEquals("Reason", stringNotNullOrEmpty(product.getTaxExemptionReason()), needsExemptionReason);
        return;
    }

    /**
     * VAT percent must be null (not 0)
     * Must have exemption reason
     * Reverse must be false
     * Intra community must be false
     * 
     * @param product
     */
    private void checkTaxCodeO(final IZUGFeRDExportableProduct product) {
        final String expectedCode = "O";
        final boolean expectedIsReverse = false;
        final boolean expectedIsIntraCommunitySupply = false;
        final boolean needsExemptionReason = true;

        BigDecimal vatPercent = null;
        try {
            vatPercent = product.getVATPercent();
        } catch (Exception e) {
            System.out.println(e.toString());
            vatPercent = null;
        }
        System.out.println(String.format("[%s] %s - %s (perc=%s rev=%s inter=%s)", product.getSellerAssignedID(),
                product.getTaxCategoryCode(), product.getTaxExemptionReason(),
                vatPercent == null ? "-NULL-" : vatPercent, product.isReverseCharge(),
                product.isIntraCommunitySupply()));
        assertEquals("Code", product.getTaxCategoryCode(), expectedCode);
        assertNull("Percent", vatPercent);
        assertEquals("Reverse", product.isReverseCharge(), expectedIsReverse);
        assertEquals("Intra", product.isIntraCommunitySupply(), expectedIsIntraCommunitySupply);
        assertEquals("Reason", stringNotNullOrEmpty(product.getTaxExemptionReason()), needsExemptionReason);
        return;
    }

    /**
     * VAT Percent must be 0%
     * Must have exemption reason
     * Reverse must be false
     * Intra community must be true
     * 
     * @param product
     */
    private void checkTaxCodeK(final IZUGFeRDExportableProduct product) {
        final String expectedCode = "K";
        final BigDecimal expectedPercent = new BigDecimal(0.00);
        final boolean expectedIsReverse = false;
        final boolean expectedIsIntraCommunitySupply = true;
        final boolean needsExemptionReason = true;

        System.out.println(String.format("[%s] %s - %s (perc=%s rev=%s inter=%s)", product.getSellerAssignedID(),
                product.getTaxCategoryCode(), product.getTaxExemptionReason(), product.getVATPercent(),
                product.isReverseCharge(), product.isIntraCommunitySupply()));
        assertEquals("Code", product.getTaxCategoryCode(), expectedCode);
        assertEquals("Percent", product.getVATPercent().compareTo(expectedPercent), 0);
        assertEquals("Reverse", product.isReverseCharge(), expectedIsReverse);
        assertEquals("Intra", product.isIntraCommunitySupply(), expectedIsIntraCommunitySupply);
        assertEquals("Reason", stringNotNullOrEmpty(product.getTaxExemptionReason()), needsExemptionReason);
        return;
    }

    /**
     * Standardrate 7% or 19% or ...
     * No exemption reason allowed
     * Reverse and intra community must be false
     * 
     * @param product
     */
    private void checkTaxCodeS(final IZUGFeRDExportableProduct product) {
        final String expectedCode = "S";
        final BigDecimal expectedPercent = new BigDecimal(19.00);
        final boolean expectedIsReverse = false;
        final boolean expectedIsIntraCommunitySupply = false;
        final boolean needsExemptionReason = false;

        System.out.println(String.format("[%s] %s - %s (perc=%s rev=%s inter=%s)", product.getSellerAssignedID(),
                product.getTaxCategoryCode(), product.getTaxExemptionReason(), product.getVATPercent(),
                product.isReverseCharge(), product.isIntraCommunitySupply()));
        assertEquals("Code", product.getTaxCategoryCode(), expectedCode);
        assertEquals("Percent", product.getVATPercent().compareTo(expectedPercent), 0);
        assertEquals("Reverse", product.isReverseCharge(), expectedIsReverse);
        assertEquals("Intra", product.isIntraCommunitySupply(), expectedIsIntraCommunitySupply);
        assertEquals("Reason", stringNotNullOrEmpty(product.getTaxExemptionReason()), needsExemptionReason);
        return;
    }

    private boolean stringNotNullOrEmpty(final String value) {
        return !isNull(value) && !value.isEmpty();
    }
}
