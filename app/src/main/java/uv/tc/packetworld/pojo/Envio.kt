package uv.tc.packetworld.pojo

data class Envio(
    val idEnvio: Int? = null,
    val numeroGuia: String,
    val destinatarioNombre: String,
    val destinatarioApPaterno: String,
    val destinatarioApMaterno: String,
    val destinoCalle: String,
    val destinoNumero: String,
    val destinoColonia: String,
    val destinoCodigoPostal: String,
    val destinoCiudad: String,
    val destinoEstado: String,
    val costoTotal: Double? = null,
    val idClienteRemitente: Int? = null,
    val codigoSucursalOrigen: String? = null,
    val idConductorAsignado: Int? = null,
    val idEstatusEnvio: Int? = null,

    // Campos derivados para mostrar
    val nombreCliente: String? = null,
    val sucursalOrigen: String? = null,
    val nombreConductor: String? = null,
    val estatusEnvio: String? = null
) {
    val destino: String
        get() = "$destinoCalle $destinoNumero, $destinoColonia, CP $destinoCodigoPostal, $destinoCiudad, $destinoEstado"
}
