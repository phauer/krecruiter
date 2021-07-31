package com.phauer.krecruiter.util

import com.phauer.krecruiter.applicationApi.ApplicationDTO
import com.phauer.krecruiter.common.ApiPaths
import com.phauer.krecruiter.common.ApplicationState
import com.phauer.krecruiter.common.ExceptionControllerAdvice
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.servlet.view.InternalResourceViewResolver

fun createMockMvc(controller: Any) = MockMvcBuilders
    .standaloneSetup(controller)
    .setViewResolvers(InternalResourceViewResolver())
    .setControllerAdvice(ExceptionControllerAdvice())
    .build()

fun MockMvc.requestApplications(
    state: ApplicationState? = null
): List<ApplicationDTO> {
    val responseString = this.get(ApiPaths.applications) {
        if (state != null) {
            param("state", state.toString())
        }
    }.andExpect {
        status { is2xxSuccessful() }
        content { contentType(MediaType.APPLICATION_JSON) }
    }.andReturn().response.contentAsString
    return TestObjects.mapper.readValue(responseString, TestObjects.applicationDtoListType)
}