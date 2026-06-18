package utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.operator.color.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

public class PdfReaderUtil {


	public String validatePdfColor() throws IOException {
           File file = new File("C:/Ixigo/ixigo-flightBooking/src/test/resources/testData/TestFile_Red.pdf");
	     /* File file = new File("C:/Ixigo/ixigo-flightBooking/src/test/resources/testData/TestFile_Black.pdf"); */
        StringBuilder fullTextBuilder = new StringBuilder();

        final boolean[] isAllBlack = {true};

        try (PDDocument document = Loader.loadPDF(file)) {

            PDFTextStripper colorStripper = new PDFTextStripper() {
            	{
                    addOperator(new SetNonStrokingColor(this));
                    addOperator(new SetNonStrokingDeviceRGBColor(this));
                    addOperator(new SetNonStrokingDeviceCMYKColor(this));
                    addOperator(new SetNonStrokingDeviceGrayColor(this));
                } 
                @Override
                protected void processTextPosition(TextPosition text) {
                    String character = text.getUnicode();

                    if (character == null || character.trim().isEmpty()) {
                        super.processTextPosition(text);
                        return;
                    }

                    try {
                        PDGraphicsState gs = getGraphicsState();
                        PDColor pdColor = gs.getNonStrokingColor();
                        PDColorSpace colorSpace = pdColor.getColorSpace();
                        
                        float[] rgbComponents = colorSpace.toRGB(pdColor.getComponents());
                        
                        Color color = new Color(rgbComponents[0], rgbComponents[1], rgbComponents[2]);

                        if (color.getRed() != 0 || color.getGreen() != 0 || color.getBlue() != 0) {
                            isAllBlack[0] = false;
                            String validationMessage = String.format("[COLOR FAILURE] Page %d | Character '%s' is NOT black! Found RGB(%d, %d, %d)%n", 
                                    getCurrentPageNo(), character, color.getRed(), color.getGreen(), color.getBlue());
                            ExtentReporter.extentTest.info(validationMessage);
                            System.out.printf("[COLOR FAILURE] Page %d | Character '%s' is NOT black! Found RGB(%d, %d, %d)%n", 
                                    getCurrentPageNo(), character, color.getRed(), color.getGreen(), color.getBlue());
                        }

                    } catch (IOException e) {
                        System.err.println("Failed to read token layout graphics: " + e.getMessage());
                    }

                    super.processTextPosition(text);
                }
            };

            String extractedText = colorStripper.getText(document);
            fullTextBuilder.append(extractedText);
            
            if (isAllBlack[0]) {
                System.out.println("The black font color of the PDF content is fully validated!");
            } else {
                System.out.println("Non-black colored text layers were detected in the file.");
            }
        }

        return fullTextBuilder.toString();
    }



}
