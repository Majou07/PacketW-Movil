package uv.tc.packetworld.pojo

data class Colaborador(
    val idColaborador: Int,
    var nombre: String,
    var apellidoPaterno: String,
    var apellidoMaterno: String?,
    val numeroPersonal: String,
    var correoElectronico: String
)
