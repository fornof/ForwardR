package com.example.rob.myapplication;

import android.graphics.Bitmap;

import com.xlythe.textmanager.text.pdu.PduPart;

import java.util.ArrayList;

public class MmsMessage {
    private String text;
    private String fromAddress;
    private byte[] data;
    private String type;
    private byte[] smil;
    private Bitmap image;
    ArrayList<PduPart> pduParts;
    public MmsMessage(){
        pduParts = new ArrayList<PduPart>();
    }

    public ArrayList<PduPart> getPduParts() {
        return pduParts;
    }
    public void addPduPart(PduPart part){
        pduParts.add(part);
    }
    public void setPduParts(ArrayList<PduPart> pduParts) {
        this.pduParts = pduParts;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;

    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getSmil() {
        return smil;
    }

    public void setSmil(byte[] smil) {
        this.smil = smil;
    }
}
