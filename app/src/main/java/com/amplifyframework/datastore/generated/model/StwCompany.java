package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.temporal.Temporal;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.AuthStrategy;
import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.ModelOperation;
import com.amplifyframework.core.model.annotations.AuthRule;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the StwCompany type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "StwCompanies", type = Model.Type.USER, version = 1, authRules = {
        @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE, ModelOperation.DELETE }),
        @AuthRule(allow = AuthStrategy.PUBLIC, provider = "iam", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ })
})
public final class StwCompany implements Model {
    public static final QueryField ID = field("StwCompany", "id");
    public static final QueryField NAME = field("StwCompany", "name");
    public static final QueryField MAX_ENTRIES = field("StwCompany", "maxEntries");
    public static final QueryField RALLY_LIST = field("StwCompany", "rallyList");
    private final @ModelField(targetType="ID", isRequired = true) String id;
    private final @ModelField(targetType="String") String name;
    private final @ModelField(targetType="Int") Integer maxEntries;
    private final @ModelField(targetType="Rally") List<Rally> rallyList;
    private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime createdAt;
    private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;
    public String resolveIdentifier() {
        return id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getMaxEntries() {
        return maxEntries;
    }

    public List<Rally> getRallyList() {
        return rallyList;
    }

    public Temporal.DateTime getCreatedAt() {
        return createdAt;
    }

    public Temporal.DateTime getUpdatedAt() {
        return updatedAt;
    }

    private StwCompany(String id, String name, Integer maxEntries, List<Rally> rallyList) {
        this.id = id;
        this.name = name;
        this.maxEntries = maxEntries;
        this.rallyList = rallyList;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if(obj == null || getClass() != obj.getClass()) {
            return false;
        } else {
            StwCompany stwCompany = (StwCompany) obj;
            return ObjectsCompat.equals(getId(), stwCompany.getId()) &&
                    ObjectsCompat.equals(getName(), stwCompany.getName()) &&
                    ObjectsCompat.equals(getMaxEntries(), stwCompany.getMaxEntries()) &&
                    ObjectsCompat.equals(getRallyList(), stwCompany.getRallyList()) &&
                    ObjectsCompat.equals(getCreatedAt(), stwCompany.getCreatedAt()) &&
                    ObjectsCompat.equals(getUpdatedAt(), stwCompany.getUpdatedAt());
        }
    }

    @Override
    public int hashCode() {
        return new StringBuilder()
                .append(getId())
                .append(getName())
                .append(getMaxEntries())
                .append(getRallyList())
                .append(getCreatedAt())
                .append(getUpdatedAt())
                .toString()
                .hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("StwCompany {")
                .append("id=" + String.valueOf(getId()) + ", ")
                .append("name=" + String.valueOf(getName()) + ", ")
                .append("maxEntries=" + String.valueOf(getMaxEntries()) + ", ")
                .append("rallyList=" + String.valueOf(getRallyList()) + ", ")
                .append("createdAt=" + String.valueOf(getCreatedAt()) + ", ")
                .append("updatedAt=" + String.valueOf(getUpdatedAt()))
                .append("}")
                .toString();
    }

    public static BuildStep builder() {
        return new Builder();
    }

    /**
     * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
     * This is a convenience method to return an instance of the object with only its ID populated
     * to be used in the context of a parameter in a delete mutation or referencing a foreign key
     * in a relationship.
     * @param id the id of the existing item this instance will represent
     * @return an instance of this model with only ID populated
     */
    public static StwCompany justId(String id) {
        return new StwCompany(
                id,
                null,
                null,
                null
        );
    }

    public CopyOfBuilder copyOfBuilder() {
        return new CopyOfBuilder(id,
                name,
                maxEntries,
                rallyList);
    }
    public interface BuildStep {
        StwCompany build();
        BuildStep id(String id);
        BuildStep name(String name);
        BuildStep maxEntries(Integer maxEntries);
        BuildStep rallyList(List<Rally> rallyList);
    }


    public static class Builder implements BuildStep {
        private String id;
        private String name;
        private Integer maxEntries;
        private List<Rally> rallyList;
        @Override
        public StwCompany build() {
            String id = this.id != null ? this.id : UUID.randomUUID().toString();

            return new StwCompany(
                    id,
                    name,
                    maxEntries,
                    rallyList);
        }

        @Override
        public BuildStep name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public BuildStep maxEntries(Integer maxEntries) {
            this.maxEntries = maxEntries;
            return this;
        }

        @Override
        public BuildStep rallyList(List<Rally> rallyList) {
            this.rallyList = rallyList;
            return this;
        }

        /**
         * @param id id
         * @return Current Builder instance, for fluent method chaining
         */
        public BuildStep id(String id) {
            this.id = id;
            return this;
        }
    }


    public final class CopyOfBuilder extends Builder {
        private CopyOfBuilder(String id, String name, Integer maxEntries, List<Rally> rallyList) {
            super.id(id);
            super.name(name)
                    .maxEntries(maxEntries)
                    .rallyList(rallyList);
        }

        @Override
        public CopyOfBuilder name(String name) {
            return (CopyOfBuilder) super.name(name);
        }

        @Override
        public CopyOfBuilder maxEntries(Integer maxEntries) {
            return (CopyOfBuilder) super.maxEntries(maxEntries);
        }

        @Override
        public CopyOfBuilder rallyList(List<Rally> rallyList) {
            return (CopyOfBuilder) super.rallyList(rallyList);
        }
    }

}
