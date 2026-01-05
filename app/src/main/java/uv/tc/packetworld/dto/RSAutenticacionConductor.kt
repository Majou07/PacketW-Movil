package uv.tc.packetworld.dto

import uv.tc.packetworld.pojo.Colaborador

data class RSAutenticacionConductor(
    val error: Boolean,
    val mensaje: String,
    val conductor: Colaborador?
)
