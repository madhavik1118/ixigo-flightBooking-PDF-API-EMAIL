package stepDefinitions;

import java.io.IOException;

import io.cucumber.java.en.Given;
import utils.PdfReaderUtil;


public class PdfValidationSteps {

	public PdfValidationSteps() {
		// TODO Auto-generated constructor stub
	}

	@Given("I load the pdf and validate the font color")
	public void validateThePdf() throws IOException {
		try {
			PdfReaderUtil pdf = new PdfReaderUtil();

			pdf.validatePdfColor();
		} catch (Exception e) {
			System.err.println("The validation execution process encountered an error: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
