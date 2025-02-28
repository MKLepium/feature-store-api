/*
 *  Copyright (c) 2020-2023. Hopsworks AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.logicalclocks.hsfs.metadata;

import com.damnhandy.uri.template.UriTemplate;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logicalclocks.hsfs.EntityEndpointType;
import com.logicalclocks.hsfs.FeatureGroupBase;
import com.logicalclocks.hsfs.FeatureStoreException;
import com.logicalclocks.hsfs.FeatureViewBase;
import com.logicalclocks.hsfs.TrainingDatasetBase;
import lombok.NonNull;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TagsApi {

  public static final String ENTITY_ROOT_PATH = "{/entityType}";
  public static final String ENTITY_ID_PATH = ENTITY_ROOT_PATH + "{/entityId}";
  public static final String TAGS_PATH = ENTITY_ID_PATH + "/tags{/name}{?value}";
  public static final String FV_TAGS_PATH = HopsworksClient.PROJECT_PATH + FeatureStoreApi.FEATURE_STORE_PATH
      + "/featureview{/fvName}/version{/fvVersion}/tags{/name}";
  public static final String FV_TD_TAGS_PATH = HopsworksClient.PROJECT_PATH + FeatureStoreApi.FEATURE_STORE_PATH
      + "/featureview{/fvName}/version{/fvVersion}"
      + "/trainingdatasets/version{/tdVersion}/tags{/name}";

  private static final Logger LOGGER = LoggerFactory.getLogger(TagsApi.class);

  private EntityEndpointType entityType;

  public TagsApi(@NonNull EntityEndpointType entityType) {
    this.entityType = entityType;
  }

  private void add(Integer projectId, Integer featurestoreId, Integer entityId, String name, Object value)
      throws FeatureStoreException, IOException {

    String pathTemplate = HopsworksClient.PROJECT_PATH
        + FeatureStoreApi.FEATURE_STORE_PATH
        + TAGS_PATH;

    UriTemplate uriTemplate = UriTemplate.fromTemplate(pathTemplate)
        .set("projectId", projectId)
        .set("fsId", featurestoreId)
        .set("entityType", entityType.getValue())
        .set("entityId", entityId)
        .set("name", name);

    add(uriTemplate, value);
  }

  private void add(UriTemplate uriTemplate, Object value)
      throws FeatureStoreException, IOException {
    HopsworksClient hopsworksClient = HopsworksClient.getInstance();

    LOGGER.info("Sending metadata request: " + uriTemplate.expand());
    HttpPut putRequest = new HttpPut(uriTemplate.expand());
    putRequest.setEntity(hopsworksClient.buildStringEntity(value));
    hopsworksClient.handleRequest(putRequest);
  }

  public void add(FeatureGroupBase featureGroupBase, String name, Object value)
      throws FeatureStoreException, IOException {
    add(featureGroupBase.getFeatureStore().getProjectId(), featureGroupBase.getFeatureStore().getId(),
        featureGroupBase.getId(), name, value);
  }

  public void add(TrainingDatasetBase trainingDatasetBase, String name, Object value)
      throws FeatureStoreException, IOException {
    add(trainingDatasetBase.getFeatureStore().getProjectId(), trainingDatasetBase.getFeatureStore().getId(),
        trainingDatasetBase.getId(), name, value);
  }

  public void add(FeatureViewBase featureViewBase, String name, Object value)
      throws FeatureStoreException, IOException {
    add(getFvUriTemplate(featureViewBase, name), value);
  }

  public void add(FeatureViewBase featureViewBase, Integer trainingDatasetVersion, String name, Object value)
      throws FeatureStoreException, IOException {
    add(getFvTdUriTemplate(featureViewBase, trainingDatasetVersion, name), value);
  }

  private UriTemplate getFvUriTemplate(FeatureViewBase featureViewBase) {
    return UriTemplate.fromTemplate(FV_TAGS_PATH)
        .set("projectId", featureViewBase.getFeatureStore().getProjectId())
        .set("fsId", featureViewBase.getFeatureStore().getId())
        .set("fvName", featureViewBase.getName())
        .set("fvVersion", featureViewBase.getVersion());
  }

  private UriTemplate getFvUriTemplate(FeatureViewBase featureViewBase, String tagName) {
    return UriTemplate.fromTemplate(FV_TAGS_PATH)
        .set("projectId", featureViewBase.getFeatureStore().getProjectId())
        .set("fsId", featureViewBase.getFeatureStore().getId())
        .set("fvName", featureViewBase.getName())
        .set("fvVersion", featureViewBase.getVersion())
        .set("name", tagName);
  }

  private UriTemplate getFvTdUriTemplate(FeatureViewBase featureViewBase, Integer trainingDatasetVersion) {
    return UriTemplate.fromTemplate(FV_TD_TAGS_PATH)
        .set("projectId", featureViewBase.getFeatureStore().getProjectId())
        .set("fsId", featureViewBase.getFeatureStore().getId())
        .set("fvName", featureViewBase.getName())
        .set("fvVersion", featureViewBase.getVersion())
        .set("tdVersion", trainingDatasetVersion);
  }

  private UriTemplate getFvTdUriTemplate(FeatureViewBase featureViewBase, Integer trainingDatasetVersion,
                                         String tagName) {
    return UriTemplate.fromTemplate(FV_TD_TAGS_PATH)
        .set("projectId", featureViewBase.getFeatureStore().getProjectId())
        .set("fsId", featureViewBase.getFeatureStore().getId())
        .set("fvName", featureViewBase.getName())
        .set("fvVersion", featureViewBase.getVersion())
        .set("tdVersion", trainingDatasetVersion)
        .set("name", tagName);
  }

  private Map<String, Object> get(Integer projectId, Integer featurestoreId, Integer entityId, Optional<String> name)
      throws FeatureStoreException, IOException {
    String pathTemplate = HopsworksClient.PROJECT_PATH
        + FeatureStoreApi.FEATURE_STORE_PATH
        + TAGS_PATH;

    UriTemplate uriTemplate = UriTemplate.fromTemplate(pathTemplate)
        .set("projectId", projectId)
        .set("fsId", featurestoreId)
        .set("entityType", entityType.getValue())
        .set("entityId", entityId);

    return get(uriTemplate, name);
  }

  private Map<String, Object> get(UriTemplate uriTemplate, Optional<String> name)
      throws FeatureStoreException, IOException {
    if (name.isPresent()) {
      uriTemplate.set("name", name.get());
    }

    String uri = uriTemplate.expand();

    LOGGER.info("Sending metadata request: " + uri);
    HttpGet getRequest = new HttpGet(uri);
    HopsworksClient hopsworksClient = HopsworksClient.getInstance();
    Map<String, Object> tags = new HashMap<>();
    for (Tags tag : hopsworksClient.handleRequest(getRequest, Tags.class).getItems()) {
      tags.put(tag.getName(), parseTagValue(hopsworksClient.getObjectMapper(), tag.getValue()));
    }
    return tags;
  }

  public Map<String, Object> get(FeatureViewBase featureViewBase)
      throws FeatureStoreException, IOException {
    return get(getFvUriTemplate(featureViewBase), Optional.empty());
  }

  public Map<String, Object> get(FeatureViewBase featureViewBase, Integer trainingDatasetVersion)
      throws FeatureStoreException, IOException {
    return get(getFvTdUriTemplate(featureViewBase, trainingDatasetVersion), Optional.empty());
  }

  public Object get(FeatureViewBase featureViewBase, String name)
      throws FeatureStoreException, IOException {
    return get(getFvUriTemplate(featureViewBase), Optional.of(name)).get(name);
  }

  public Object get(FeatureViewBase featureViewBase, Integer trainingDatasetVersion, String name)
      throws FeatureStoreException, IOException {
    return get(getFvTdUriTemplate(featureViewBase, trainingDatasetVersion), Optional.of(name)).get(name);
  }

  public Object get(FeatureGroupBase featureGroupBase, String name) throws FeatureStoreException, IOException {
    return get(featureGroupBase.getFeatureStore().getProjectId(),
        featureGroupBase.getFeatureStore().getId(), featureGroupBase.getId(), Optional.of(name))
        .get(name);
  }

  public Object get(TrainingDatasetBase trainingDatasetBase, String name) throws FeatureStoreException, IOException {
    return get(trainingDatasetBase.getFeatureStore().getProjectId(),
        trainingDatasetBase.getFeatureStore().getId(), trainingDatasetBase.getId(), Optional.of(name))
        .get(name);
  }

  public Map<String, Object> get(FeatureGroupBase featureGroupBase) throws FeatureStoreException, IOException {
    return get(featureGroupBase.getFeatureStore().getProjectId(),
      featureGroupBase.getFeatureStore().getId(), featureGroupBase.getId(), Optional.empty());
  }

  public Map<String, Object> get(TrainingDatasetBase trainingDatasetBase) throws FeatureStoreException, IOException {
    return get(trainingDatasetBase.getFeatureStore().getProjectId(),
      trainingDatasetBase.getFeatureStore().getId(), trainingDatasetBase.getId(), Optional.empty());
  }

  public Object parseTagValue(ObjectMapper objectMapper, Object value) throws IOException {
    if (value instanceof Double || value instanceof Integer) {
      return value;
    }
    String val = (String)value;
    try {
      return objectMapper.readValue(val, Map.class);
    } catch (JsonParseException | JsonMappingException e1) {
      try {
        return objectMapper.readValue(val, Object[].class);
      } catch (JsonParseException | JsonMappingException e2) {
        return val;
      }
    }
  }

  private void deleteTag(Integer projectId, Integer featurestoreId, Integer entityId, String name)
      throws FeatureStoreException, IOException {
    String pathTemplate = HopsworksClient.PROJECT_PATH
        + FeatureStoreApi.FEATURE_STORE_PATH
        + TAGS_PATH;

    UriTemplate uriTemplate = UriTemplate.fromTemplate(pathTemplate)
        .set("projectId", projectId)
        .set("fsId", featurestoreId)
        .set("entityType", entityType.getValue())
        .set("entityId", entityId)
        .set("name", name);

    deleteTag(uriTemplate);
  }

  private void deleteTag(UriTemplate uriTemplate)
      throws FeatureStoreException, IOException {
    HopsworksClient hopsworksClient = HopsworksClient.getInstance();
    LOGGER.info("Sending metadata request: " + uriTemplate.expand());
    HttpDelete httpDelete = new HttpDelete(uriTemplate.expand());
    hopsworksClient.handleRequest(httpDelete);
  }

  public void deleteTag(FeatureViewBase featureViewBase, String name)
      throws FeatureStoreException, IOException {
    deleteTag(getFvUriTemplate(featureViewBase, name));
  }

  public void deleteTag(FeatureViewBase featureViewBase, Integer trainingDatasetVersion, String name)
      throws FeatureStoreException, IOException {
    deleteTag(getFvTdUriTemplate(featureViewBase, trainingDatasetVersion, name));
  }

  public void deleteTag(FeatureGroupBase featureGroup, String name) throws FeatureStoreException, IOException {
    deleteTag(featureGroup.getFeatureStore().getProjectId(), featureGroup.getFeatureStore().getId(),
        featureGroup.getId(), name);
  }

  public void deleteTag(TrainingDatasetBase trainingDatasetBase, String name)
      throws FeatureStoreException, IOException {
    deleteTag(trainingDatasetBase.getFeatureStore().getProjectId(),
        trainingDatasetBase.getFeatureStore().getId(),
        trainingDatasetBase.getId(), name);
  }
}
