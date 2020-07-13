package com.progresssoft.fxdealimport;

import com.progresssoft.fxdealimport.controller.FxController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import static junit.framework.TestCase.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@RunWith(SpringRunner.class)
//@WebMvcTest(FxController.class)
public class FXDealImportApplicationTests {

	private MockMvc mockMvc;

	@Autowired
	WebApplicationContext wContext;

	@MockBean
	private FxController fxController;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wContext)
				.alwaysDo(MockMvcResultHandlers.print())
				.build();
	}

	@Test
	public void test() throws Exception {
		// Mock Request
		Resource fileResource = new ClassPathResource(
				"MOCK_DATA.csv");
		assertNotNull(fileResource);
		MockMultipartFile firstFile = new MockMultipartFile(
				"file",fileResource.getFilename(),
				MediaType.MULTIPART_FORM_DATA_VALUE,
				fileResource.getInputStream());
		assertNotNull(firstFile);

		MockMvc mockMvc = MockMvcBuilders.
				webAppContextSetup(webApplicationContext).build();

		mockMvc.perform(MockMvcRequestBuilders
				.fileUpload("/app/fxdeal/v1/fx")
				.file(firstFile)
				.param("filename", fileResource.getFilename()))
				.andExpect(status().is(200));
	}
}



