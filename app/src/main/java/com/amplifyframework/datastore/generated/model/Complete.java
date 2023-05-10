package com.amplifyframework.datastore.generated.model;


import androidx.core.util.ObjectsCompat;

import java.util.Objects;
import java.util.List;

/** This is an auto generated class representing the complete type in your schema. */
public final class Complete {
  private final String cnId;
  private final String srId;
  private final Integer history;
  private final List<CheckPoint> cp;
  public String getCnId() {
      return cnId;
  }
  
  public String getSrId() {
      return srId;
  }
  
  public Integer getHistory() {
      return history;
  }
  
  public List<CheckPoint> getCp() {
      return cp;
  }
  
  private Complete(String cnId, String srId, Integer history, List<CheckPoint> cp) {
    this.cnId = cnId;
    this.srId = srId;
    this.history = history;
    this.cp = cp;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Complete complete = (Complete) obj;
      return ObjectsCompat.equals(getCnId(), complete.getCnId()) &&
              ObjectsCompat.equals(getSrId(), complete.getSrId()) &&
              ObjectsCompat.equals(getHistory(), complete.getHistory()) &&
              ObjectsCompat.equals(getCp(), complete.getCp());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getCnId())
      .append(getSrId())
      .append(getHistory())
      .append(getCp())
      .toString()
      .hashCode();
  }
  
  public static CnIdStep builder() {
      return new Builder();
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(cnId,
      srId,
      history,
      cp);
  }
  public interface CnIdStep {
    SrIdStep cnId(String cnId);
  }
  

  public interface SrIdStep {
    HistoryStep srId(String srId);
  }
  

  public interface HistoryStep {
    BuildStep history(Integer history);
  }
  

  public interface BuildStep {
    Complete build();
    BuildStep cp(List<CheckPoint> cp);
  }
  

  public static class Builder implements CnIdStep, SrIdStep, HistoryStep, BuildStep {
    private String cnId;
    private String srId;
    private Integer history;
    private List<CheckPoint> cp;
    @Override
     public Complete build() {
        
        return new Complete(
          cnId,
          srId,
          history,
          cp);
    }
    
    @Override
     public SrIdStep cnId(String cnId) {
        Objects.requireNonNull(cnId);
        this.cnId = cnId;
        return this;
    }
    
    @Override
     public HistoryStep srId(String srId) {
        Objects.requireNonNull(srId);
        this.srId = srId;
        return this;
    }
    
    @Override
     public BuildStep history(Integer history) {
        Objects.requireNonNull(history);
        this.history = history;
        return this;
    }
    
    @Override
     public BuildStep cp(List<CheckPoint> cp) {
        this.cp = cp;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String cnId, String srId, Integer history, List<CheckPoint> cp) {
      super.cnId(cnId)
        .srId(srId)
        .history(history)
        .cp(cp);
    }
    
    @Override
     public CopyOfBuilder cnId(String cnId) {
      return (CopyOfBuilder) super.cnId(cnId);
    }
    
    @Override
     public CopyOfBuilder srId(String srId) {
      return (CopyOfBuilder) super.srId(srId);
    }
    
    @Override
     public CopyOfBuilder history(Integer history) {
      return (CopyOfBuilder) super.history(history);
    }
    
    @Override
     public CopyOfBuilder cp(List<CheckPoint> cp) {
      return (CopyOfBuilder) super.cp(cp);
    }
  }
  
}
