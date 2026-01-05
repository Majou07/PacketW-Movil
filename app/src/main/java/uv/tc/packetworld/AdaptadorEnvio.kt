package uv.tc.packetworld

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import uv.tc.packetworld.pojo.Envio

class AdaptadorEnvio (
    context: Context,
    private val envios: List<Envio>
) : ArrayAdapter<Envio>(context, 0, envios) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)

        val envio = envios[position]

        val text1 = view.findViewById<TextView>(android.R.id.text1)
        val text2 = view.findViewById<TextView>(android.R.id.text2)

        text1.text = envio.numeroGuia
        text2.text = "${envio.destino} | ${envio.estatus}"

        return view
    }
}