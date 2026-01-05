package uv.tc.packetworld.pojo

data class Colaborador(
    val idColaborador: Int,
    val nombre: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String?,
    val numeroPersonal: String,
    val correoElectronico: String
)
