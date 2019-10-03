package com.phauer.recruitingapp.applicationApi

import com.phauer.recruitingapp.common.ApplicationEntity
import org.jdbi.v3.sqlobject.SqlObject
import org.jdbi.v3.sqlobject.statement.SqlQuery

interface ApplicationDAO : SqlObject {

    @SqlQuery(
        """
        SELECT * FROM application
    """
    )
    fun findAllApplications(): List<ApplicationEntity>

}