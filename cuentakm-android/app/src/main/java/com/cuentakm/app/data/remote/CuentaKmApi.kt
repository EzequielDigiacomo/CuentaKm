package com.cuentakm.app.data.remote

import com.cuentakm.app.data.remote.dto.AuthResponseDto
import com.cuentakm.app.data.remote.dto.CreateMaintenanceRequestDto
import com.cuentakm.app.data.remote.dto.GoogleAuthRequest
import com.cuentakm.app.data.remote.dto.MaintenanceDto
import com.cuentakm.app.data.remote.dto.UpdateKmRequestDto
import com.cuentakm.app.data.remote.dto.VehicleSummaryDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Endpoints reales de `CuentaKm.Api` (sin inventar rutas nuevas). */
interface CuentaKmApi {

    @POST("api/auth/google")
    suspend fun signInWithGoogle(@Body body: GoogleAuthRequest): AuthResponseDto

    @GET("api/vehicles/{patente}")
    suspend fun vehicleSummary(@Path("patente") patente: String): VehicleSummaryDto

    @GET("api/vehicles/{patente}/maintenances")
    suspend fun maintenanceHistory(@Path("patente") patente: String): List<MaintenanceDto>

    @POST("api/vehicles/{patente}/maintenances")
    suspend fun createMaintenance(
        @Path("patente") patente: String,
        @Body body: CreateMaintenanceRequestDto,
    ): MaintenanceDto

    @DELETE("api/vehicles/{patente}/maintenances/{id}")
    suspend fun deleteMaintenance(
        @Path("patente") patente: String,
        @Path("id") id: String,
    )

    @PUT("api/vehicles/{patente}/km")
    suspend fun updateKm(
        @Path("patente") patente: String,
        @Body body: UpdateKmRequestDto,
    ): VehicleSummaryDto
}
