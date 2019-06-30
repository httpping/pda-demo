package com.olc.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Arrays;

public class InventoryModel implements Serializable {

    private int count;

    private byte[] PC;
    private String PCHEX = "";

    private byte[] EPC;
    private String EPCHEX = "";

    private byte RSSI = -1;
    private String RSSIHEX = "";

    private byte AntID = 1;
    private String AntIDHEX = "";

    private byte[] ReadRate;
    private String ReadRateHEX = "";

    private byte[] TotalRead;
    private int TotalReadDEC = -1;

    private byte ErrorCode = 0;
    private String ErrorCodeHEX = "";

    private byte[] TID;
    private String TIDHEX;

    public InventoryModel() {
        super();
    }

    public byte[] getPC() {
        return PC;
    }

    public void setPC(byte[] PC) {
        this.PC = PC;
    }

    public String getPCHEX() {
        return PCHEX;
    }

    public void setPCHEX(String PCHEX) {
        this.PCHEX = PCHEX;
    }

    public byte[] getEPC() {
        return EPC;
    }

    public void setEPC(byte[] EPC) {
        this.EPC = EPC;
    }

    public String getEPCHEX() {
        return EPCHEX;
    }

    public void setEPCHEX(String EPCHEX) {
        this.EPCHEX = EPCHEX;
    }

    public byte getRSSI() {
        return RSSI;
    }

    public void setRSSI(byte RSSI) {
        this.RSSI = RSSI;
    }

    public String getRSSIHEX() {
        return RSSIHEX;
    }

    public void setRSSIHEX(String RSSIHEX) {
        this.RSSIHEX = RSSIHEX;
    }

    public byte getAntID() {
        return AntID;
    }

    public void setAntID(byte antID) {
        AntID = antID;
    }

    public String getAntIDHEX() {
        return AntIDHEX;
    }

    public void setAntIDHEX(String antIDHEX) {
        AntIDHEX = antIDHEX;
    }

    public byte[] getReadRate() {
        return ReadRate;
    }

    public void setReadRate(byte[] readRate) {
        ReadRate = readRate;
    }

    public String getReadRateHEX() {
        return ReadRateHEX;
    }

    public void setReadRateHEX(String readRateHEX) {
        ReadRateHEX = readRateHEX;
    }

    public byte[] getTotalRead() {
        return TotalRead;
    }

    public void setTotalRead(byte[] totalRead) {
        TotalRead = totalRead;
    }

    public int getTotalReadDEC() {
        return TotalReadDEC;
    }

    public void setTotalReadDEC(int totalReadDEC) {
        TotalReadDEC = totalReadDEC;
    }

    public byte getErrorCode() {
        return ErrorCode;
    }

    public void setErrorCode(byte errorCode) {
        ErrorCode = errorCode;
    }

    public String getErrorCodeHEX() {
        return ErrorCodeHEX;
    }

    public void setErrorCodeHEX(String errorCodeHEX) {
        ErrorCodeHEX = errorCodeHEX;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public byte[] getTID() {
        return TID;
    }

    public void setTID(byte[] TID) {
        this.TID = TID;
    }

    public String getTIDHEX() {
        return TIDHEX;
    }

    public void setTIDHEX(String TIDHEX) {
        this.TIDHEX = TIDHEX;
    }
}
