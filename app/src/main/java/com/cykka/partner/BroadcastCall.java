package com.cykka.partner;

public class BroadcastCall {
    private String callId, isActive, buySell, currencyId, cmp, entryPrice, isEdited, scripName, specType, stopLossOrHoldingPeriod, targetPrice, timeStamp, notes;

    public BroadcastCall(){}

    public BroadcastCall(String callId, String isActive, String buySell, String currencyId, String cmp, String entryPrice, String isEdited,
                         String scripName, String specType, String stopLossOrHoldingPeriod, String targetPrice, String timeStamp, String notes){
        this.callId = callId;
        this.isActive = isActive;
        this.buySell = buySell;
        this.currencyId = currencyId;
        this.cmp = cmp;
        this.entryPrice = entryPrice;
        this.isEdited = isEdited;
        this.scripName = scripName;
        this.specType = specType;
        this.stopLossOrHoldingPeriod = stopLossOrHoldingPeriod;
        this.targetPrice = targetPrice;
        this.timeStamp = timeStamp;
        this.notes = notes;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getBuySell() {
        return buySell;
    }

    public void setBuySell(String buySell) {
        this.buySell = buySell;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public String getCmp() {
        return cmp;
    }

    public void setCmp(String cmp) {
        this.cmp = cmp;
    }

    public String getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(String entryPrice) {
        this.entryPrice = entryPrice;
    }

    public String getIsEdited() {
        return isEdited;
    }

    public void setIsEdited(String isEdited) {
        this.isEdited = isEdited;
    }

    public String getScripName() {
        return scripName;
    }

    public void setScripName(String scripName) {
        this.scripName = scripName;
    }

    public String getSpecType() {
        return specType;
    }

    public void setSpecType(String specType) {
        this.specType = specType;
    }

    public String getStopLossOrHoldingPeriod() {
        return stopLossOrHoldingPeriod;
    }

    public void setStopLossOrHoldingPeriod(String stopLossOrHoldingPeriod) {
        this.stopLossOrHoldingPeriod = stopLossOrHoldingPeriod;
    }

    public String getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(String targetPrice) {
        this.targetPrice = targetPrice;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
