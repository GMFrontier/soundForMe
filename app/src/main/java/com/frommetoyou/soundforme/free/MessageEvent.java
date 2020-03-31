package com.frommetoyou.soundforme;

public class MessageEvent {

    //puedo enviar mensajes si quiero, pero NO jaja!
    private boolean mServicioDetenido, mCambio_modo;
    private String mAccion;
    public MessageEvent(Boolean servicioDetenido,Boolean cambio_modo, String accion) {
        mServicioDetenido =servicioDetenido;
        mCambio_modo=cambio_modo;
        mAccion=accion;
    }

    public Boolean getServiciDetenido() {
        return mServicioDetenido;
    }
    public Boolean getCambioModo()
    {
        return mCambio_modo;
    }
    public String getAccion() {
        return mAccion;
    }
}
