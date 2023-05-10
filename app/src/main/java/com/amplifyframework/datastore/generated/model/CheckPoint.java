package com.amplifyframework.datastore.generated.model;


import androidx.core.util.ObjectsCompat;

import java.util.Objects;
import java.util.List;

/** This is an auto generated class representing the CheckPoint type in your schema. */
public final class CheckPoint {
  private final String cpId;
  private final String cpName;
  private final String latitude;
  private final String longitude;
  public String getCpId() {
      return cpId;
  }
  
  public String getCpName() {
      return cpName;
  }
  
  public String getLatitude() {
      return latitude;
  }
  
  public String getLongitude() {
      return longitude;
  }
  
  private CheckPoint(String cpId, String cpName, String latitude, String longitude) {
    this.cpId = cpId;
    this.cpName = cpName;
    this.latitude = latitude;
    this.longitude = longitude;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      CheckPoint checkPoint = (CheckPoint) obj;
      return ObjectsCompat.equals(getCpId(), checkPoint.getCpId()) &&
              ObjectsCompat.equals(getCpName(), checkPoint.getCpName()) &&
              ObjectsCompat.equals(getLatitude(), checkPoint.getLatitude()) &&
              ObjectsCompat.equals(getLongitude(), checkPoint.getLongitude());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getCpId())
      .append(getCpName())
      .append(getLatitude())
      .append(getLongitude())
      .toString()
      .hashCode();
  }
  
  public static BuildStep builder() {
      return new Builder();
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(cpId,
      cpName,
      latitude,
      longitude);
  }
  public interface BuildStep {
    CheckPoint build();
    BuildStep cpId(String cpId);
    BuildStep cpName(String cpName);
    BuildStep latitude(String latitude);
    BuildStep longitude(String longitude);
  }
  

  public static class Builder implements BuildStep {
    private String cpId;
    private String cpName;
    private String latitude;
    private String longitude;
    @Override
     public CheckPoint build() {
        
        return new CheckPoint(
          cpId,
          cpName,
          latitude,
          longitude);
    }
    
    @Override
     public BuildStep cpId(String cpId) {
        this.cpId = cpId;
        return this;
    }
    
    @Override
     public BuildStep cpName(String cpName) {
        this.cpName = cpName;
        return this;
    }
    
    @Override
     public BuildStep latitude(String latitude) {
        this.latitude = latitude;
        return this;
    }
    
    @Override
     public BuildStep longitude(String longitude) {
        this.longitude = longitude;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String cpId, String cpName, String latitude, String longitude) {
      super.cpId(cpId)
        .cpName(cpName)
        .latitude(latitude)
        .longitude(longitude);
    }
    
    @Override
     public CopyOfBuilder cpId(String cpId) {
      return (CopyOfBuilder) super.cpId(cpId);
    }
    
    @Override
     public CopyOfBuilder cpName(String cpName) {
      return (CopyOfBuilder) super.cpName(cpName);
    }
    
    @Override
     public CopyOfBuilder latitude(String latitude) {
      return (CopyOfBuilder) super.latitude(latitude);
    }
    
    @Override
     public CopyOfBuilder longitude(String longitude) {
      return (CopyOfBuilder) super.longitude(longitude);
    }
  }
  
}
