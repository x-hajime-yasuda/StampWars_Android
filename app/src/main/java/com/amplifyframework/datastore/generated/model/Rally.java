package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.temporal.Temporal;

import androidx.core.util.ObjectsCompat;

import java.util.Objects;
import java.util.List;

/** This is an auto generated class representing the Rally type in your schema. */
public final class Rally {
  private final String srId;
  private final String title;
  private final Temporal.Timestamp startAt;
  private final Temporal.Timestamp endAt;
  private final Temporal.Timestamp displayStartAt;
  private final Temporal.Timestamp displayEndAt;
  private final String place;
  private final Integer total;
  private final String detail;
  private final Integer status;
  private final Integer completeCount;
  private final String rewardTitle;
  private final String rewardDetail;
  private final List<CheckPoint> cp;
  private final Boolean isLocationAvailable;
  private final Boolean isKeywordAvailable;
  public String getSrId() {
      return srId;
  }
  
  public String getTitle() {
      return title;
  }
  
  public Temporal.Timestamp getStartAt() {
      return startAt;
  }
  
  public Temporal.Timestamp getEndAt() {
      return endAt;
  }
  
  public Temporal.Timestamp getDisplayStartAt() {
      return displayStartAt;
  }
  
  public Temporal.Timestamp getDisplayEndAt() {
      return displayEndAt;
  }
  
  public String getPlace() {
      return place;
  }
  
  public Integer getTotal() {
      return total;
  }
  
  public String getDetail() {
      return detail;
  }
  
  public Integer getStatus() {
      return status;
  }
  
  public Integer getCompleteCount() {
      return completeCount;
  }
  
  public String getRewardTitle() {
      return rewardTitle;
  }
  
  public String getRewardDetail() {
      return rewardDetail;
  }
  
  public List<CheckPoint> getCp() {
      return cp;
  }
  
  public Boolean getIsLocationAvailable() {
      return isLocationAvailable;
  }
  
  public Boolean getIsKeywordAvailable() {
      return isKeywordAvailable;
  }
  
  private Rally(String srId, String title, Temporal.Timestamp startAt, Temporal.Timestamp endAt, Temporal.Timestamp displayStartAt, Temporal.Timestamp displayEndAt, String place, Integer total, String detail, Integer status, Integer completeCount, String rewardTitle, String rewardDetail, List<CheckPoint> cp, Boolean isLocationAvailable, Boolean isKeywordAvailable) {
    this.srId = srId;
    this.title = title;
    this.startAt = startAt;
    this.endAt = endAt;
    this.displayStartAt = displayStartAt;
    this.displayEndAt = displayEndAt;
    this.place = place;
    this.total = total;
    this.detail = detail;
    this.status = status;
    this.completeCount = completeCount;
    this.rewardTitle = rewardTitle;
    this.rewardDetail = rewardDetail;
    this.cp = cp;
    this.isLocationAvailable = isLocationAvailable;
    this.isKeywordAvailable = isKeywordAvailable;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Rally rally = (Rally) obj;
      return ObjectsCompat.equals(getSrId(), rally.getSrId()) &&
              ObjectsCompat.equals(getTitle(), rally.getTitle()) &&
              ObjectsCompat.equals(getStartAt(), rally.getStartAt()) &&
              ObjectsCompat.equals(getEndAt(), rally.getEndAt()) &&
              ObjectsCompat.equals(getDisplayStartAt(), rally.getDisplayStartAt()) &&
              ObjectsCompat.equals(getDisplayEndAt(), rally.getDisplayEndAt()) &&
              ObjectsCompat.equals(getPlace(), rally.getPlace()) &&
              ObjectsCompat.equals(getTotal(), rally.getTotal()) &&
              ObjectsCompat.equals(getDetail(), rally.getDetail()) &&
              ObjectsCompat.equals(getStatus(), rally.getStatus()) &&
              ObjectsCompat.equals(getCompleteCount(), rally.getCompleteCount()) &&
              ObjectsCompat.equals(getRewardTitle(), rally.getRewardTitle()) &&
              ObjectsCompat.equals(getRewardDetail(), rally.getRewardDetail()) &&
              ObjectsCompat.equals(getCp(), rally.getCp()) &&
              ObjectsCompat.equals(getIsLocationAvailable(), rally.getIsLocationAvailable()) &&
              ObjectsCompat.equals(getIsKeywordAvailable(), rally.getIsKeywordAvailable());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getSrId())
      .append(getTitle())
      .append(getStartAt())
      .append(getEndAt())
      .append(getDisplayStartAt())
      .append(getDisplayEndAt())
      .append(getPlace())
      .append(getTotal())
      .append(getDetail())
      .append(getStatus())
      .append(getCompleteCount())
      .append(getRewardTitle())
      .append(getRewardDetail())
      .append(getCp())
      .append(getIsLocationAvailable())
      .append(getIsKeywordAvailable())
      .toString()
      .hashCode();
  }
  
  public static BuildStep builder() {
      return new Builder();
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(srId,
      title,
      startAt,
      endAt,
      displayStartAt,
      displayEndAt,
      place,
      total,
      detail,
      status,
      completeCount,
      rewardTitle,
      rewardDetail,
      cp,
      isLocationAvailable,
      isKeywordAvailable);
  }
  public interface BuildStep {
    Rally build();
    BuildStep srId(String srId);
    BuildStep title(String title);
    BuildStep startAt(Temporal.Timestamp startAt);
    BuildStep endAt(Temporal.Timestamp endAt);
    BuildStep displayStartAt(Temporal.Timestamp displayStartAt);
    BuildStep displayEndAt(Temporal.Timestamp displayEndAt);
    BuildStep place(String place);
    BuildStep total(Integer total);
    BuildStep detail(String detail);
    BuildStep status(Integer status);
    BuildStep completeCount(Integer completeCount);
    BuildStep rewardTitle(String rewardTitle);
    BuildStep rewardDetail(String rewardDetail);
    BuildStep cp(List<CheckPoint> cp);
    BuildStep isLocationAvailable(Boolean isLocationAvailable);
    BuildStep isKeywordAvailable(Boolean isKeywordAvailable);
  }
  

  public static class Builder implements BuildStep {
    private String srId;
    private String title;
    private Temporal.Timestamp startAt;
    private Temporal.Timestamp endAt;
    private Temporal.Timestamp displayStartAt;
    private Temporal.Timestamp displayEndAt;
    private String place;
    private Integer total;
    private String detail;
    private Integer status;
    private Integer completeCount;
    private String rewardTitle;
    private String rewardDetail;
    private List<CheckPoint> cp;
    private Boolean isLocationAvailable;
    private Boolean isKeywordAvailable;
    @Override
     public Rally build() {
        
        return new Rally(
          srId,
          title,
          startAt,
          endAt,
          displayStartAt,
          displayEndAt,
          place,
          total,
          detail,
          status,
          completeCount,
          rewardTitle,
          rewardDetail,
          cp,
          isLocationAvailable,
          isKeywordAvailable);
    }
    
    @Override
     public BuildStep srId(String srId) {
        this.srId = srId;
        return this;
    }
    
    @Override
     public BuildStep title(String title) {
        this.title = title;
        return this;
    }
    
    @Override
     public BuildStep startAt(Temporal.Timestamp startAt) {
        this.startAt = startAt;
        return this;
    }
    
    @Override
     public BuildStep endAt(Temporal.Timestamp endAt) {
        this.endAt = endAt;
        return this;
    }
    
    @Override
     public BuildStep displayStartAt(Temporal.Timestamp displayStartAt) {
        this.displayStartAt = displayStartAt;
        return this;
    }
    
    @Override
     public BuildStep displayEndAt(Temporal.Timestamp displayEndAt) {
        this.displayEndAt = displayEndAt;
        return this;
    }
    
    @Override
     public BuildStep place(String place) {
        this.place = place;
        return this;
    }
    
    @Override
     public BuildStep total(Integer total) {
        this.total = total;
        return this;
    }
    
    @Override
     public BuildStep detail(String detail) {
        this.detail = detail;
        return this;
    }
    
    @Override
     public BuildStep status(Integer status) {
        this.status = status;
        return this;
    }
    
    @Override
     public BuildStep completeCount(Integer completeCount) {
        this.completeCount = completeCount;
        return this;
    }
    
    @Override
     public BuildStep rewardTitle(String rewardTitle) {
        this.rewardTitle = rewardTitle;
        return this;
    }
    
    @Override
     public BuildStep rewardDetail(String rewardDetail) {
        this.rewardDetail = rewardDetail;
        return this;
    }
    
    @Override
     public BuildStep cp(List<CheckPoint> cp) {
        this.cp = cp;
        return this;
    }
    
    @Override
     public BuildStep isLocationAvailable(Boolean isLocationAvailable) {
        this.isLocationAvailable = isLocationAvailable;
        return this;
    }
    
    @Override
     public BuildStep isKeywordAvailable(Boolean isKeywordAvailable) {
        this.isKeywordAvailable = isKeywordAvailable;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String srId, String title, Temporal.Timestamp startAt, Temporal.Timestamp endAt, Temporal.Timestamp displayStartAt, Temporal.Timestamp displayEndAt, String place, Integer total, String detail, Integer status, Integer completeCount, String rewardTitle, String rewardDetail, List<CheckPoint> cp, Boolean isLocationAvailable, Boolean isKeywordAvailable) {
      super.srId(srId)
        .title(title)
        .startAt(startAt)
        .endAt(endAt)
        .displayStartAt(displayStartAt)
        .displayEndAt(displayEndAt)
        .place(place)
        .total(total)
        .detail(detail)
        .status(status)
        .completeCount(completeCount)
        .rewardTitle(rewardTitle)
        .rewardDetail(rewardDetail)
        .cp(cp)
        .isLocationAvailable(isLocationAvailable)
        .isKeywordAvailable(isKeywordAvailable);
    }
    
    @Override
     public CopyOfBuilder srId(String srId) {
      return (CopyOfBuilder) super.srId(srId);
    }
    
    @Override
     public CopyOfBuilder title(String title) {
      return (CopyOfBuilder) super.title(title);
    }
    
    @Override
     public CopyOfBuilder startAt(Temporal.Timestamp startAt) {
      return (CopyOfBuilder) super.startAt(startAt);
    }
    
    @Override
     public CopyOfBuilder endAt(Temporal.Timestamp endAt) {
      return (CopyOfBuilder) super.endAt(endAt);
    }
    
    @Override
     public CopyOfBuilder displayStartAt(Temporal.Timestamp displayStartAt) {
      return (CopyOfBuilder) super.displayStartAt(displayStartAt);
    }
    
    @Override
     public CopyOfBuilder displayEndAt(Temporal.Timestamp displayEndAt) {
      return (CopyOfBuilder) super.displayEndAt(displayEndAt);
    }
    
    @Override
     public CopyOfBuilder place(String place) {
      return (CopyOfBuilder) super.place(place);
    }
    
    @Override
     public CopyOfBuilder total(Integer total) {
      return (CopyOfBuilder) super.total(total);
    }
    
    @Override
     public CopyOfBuilder detail(String detail) {
      return (CopyOfBuilder) super.detail(detail);
    }
    
    @Override
     public CopyOfBuilder status(Integer status) {
      return (CopyOfBuilder) super.status(status);
    }
    
    @Override
     public CopyOfBuilder completeCount(Integer completeCount) {
      return (CopyOfBuilder) super.completeCount(completeCount);
    }
    
    @Override
     public CopyOfBuilder rewardTitle(String rewardTitle) {
      return (CopyOfBuilder) super.rewardTitle(rewardTitle);
    }
    
    @Override
     public CopyOfBuilder rewardDetail(String rewardDetail) {
      return (CopyOfBuilder) super.rewardDetail(rewardDetail);
    }
    
    @Override
     public CopyOfBuilder cp(List<CheckPoint> cp) {
      return (CopyOfBuilder) super.cp(cp);
    }
    
    @Override
     public CopyOfBuilder isLocationAvailable(Boolean isLocationAvailable) {
      return (CopyOfBuilder) super.isLocationAvailable(isLocationAvailable);
    }
    
    @Override
     public CopyOfBuilder isKeywordAvailable(Boolean isKeywordAvailable) {
      return (CopyOfBuilder) super.isKeywordAvailable(isKeywordAvailable);
    }
  }
  
}
