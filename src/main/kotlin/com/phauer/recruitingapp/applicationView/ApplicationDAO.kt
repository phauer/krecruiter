package com.phauer.recruitingapp.applicationView

import com.phauer.recruitingapp.common.AddressEntity
import com.phauer.recruitingapp.common.ApplicantEntity
import com.phauer.recruitingapp.common.ApplicationEntity
import com.phauer.recruitingapp.common.ApplicationState
import org.springframework.stereotype.Component

@Component
class ApplicationDAO (

) {

    fun findAllApplications(): List<ApplicationEntity> {
        // TODO access real db
        return listOf(
            ApplicationEntity(
                id = 1,
                applicant = ApplicantEntity(
                    firstName = "Jon",
                    lastName = "Snow",
                    address = AddressEntity(
                        street = "Castle Street 1",
                        city = "Winterfell",
                        zipCode = "123412"
                    )
                ),
                jobTitle = "Software Developer",
                status = ApplicationState.EMPLOYED,
                isInternalApplication = false
           ),
            ApplicationEntity(
                id = 2,
                applicant = ApplicantEntity(
                    firstName = "Cersei",
                    lastName = "Lannister",
                    address = AddressEntity(
                        street = "Main Street 1",
                        city = "King's Landing",
                        zipCode = "123412"
                    )
                ),
                jobTitle = "Scrum Master",
                status = ApplicationState.REJECTED,
                isInternalApplication = false
            )
        )
    }
}