package com.phauer.recruitingapp.applicationView

import com.phauer.recruitingapp.common.ApplicationEntity
import com.phauer.recruitingapp.common.ApplicationState
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.stereotype.Component

@Route(value = "applicants", layout = MainLayout::class)
@PageTitle("Recruiting App")
class ApplicationView(
    dao: ApplicationDAO
) : VerticalLayout() {

    // TODO better use plain HTML?

    init {
        add(H1("Applicants"))

        val applicationEntities = dao.findAllApplications()
        val applicationModels = mapToModels(applicationEntities)
        val applicationGrid = Grid<ApplicationModel>(ApplicationModel::class.java)
        applicationGrid.setItems(applicationModels)
        add(applicationGrid)
    }

    private fun mapToModels(entities: List<ApplicationEntity>) = entities.map(this::mapToModel)

    private fun mapToModel(entity: ApplicationEntity) = ApplicationModel(
        applicatName = "${entity.applicant.firstName} ${entity.applicant.lastName}",
        jobTitle = entity.jobTitle,
        status = entity.status
    )
}

data class ApplicationModel(
    val applicatName: String,
    val jobTitle: String,
    val status: ApplicationState
)