package uv.tc.packetworld.pojo

data class Colaborador(
    var idColaborador: Int,
    var numeroPersonal: String,
    var nombre: String,
    var apellidoPaterno: String,
    var apellidoMaterno: String,
    var curp: String?,
    var correoElectronico: String?,
    var contrasena: String?,
    var fotografia: String?,
    var numeroLicencia: String?,
    var idRol: Int,
    var rol: String,
    var codigoSucursal: String?,
    var sucursal: String?,
    var idUnidadAsignada: Int?
)
