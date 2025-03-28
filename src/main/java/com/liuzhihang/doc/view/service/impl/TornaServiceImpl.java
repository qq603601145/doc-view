package com.liuzhihang.doc.view.service.impl;

import cn.torna.sdk.common.Booleans;
import cn.torna.sdk.param.DebugEnv;
import cn.torna.sdk.param.DocItem;
import cn.torna.sdk.param.DocParamHeader;
import cn.torna.sdk.param.DocParamReq;
import cn.torna.sdk.param.DocParamResp;
import cn.torna.sdk.request.DocPushRequest;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.liuzhihang.doc.view.DocViewBundle;
import com.liuzhihang.doc.view.config.TornaSettings;
import com.liuzhihang.doc.view.config.TornaSettingsConfigurable;
import com.liuzhihang.doc.view.config.YApiSettings;
import com.liuzhihang.doc.view.constant.FieldTypeConstant;
import com.liuzhihang.doc.view.dto.Body;
import com.liuzhihang.doc.view.dto.DocView;
import com.liuzhihang.doc.view.dto.Header;
import com.liuzhihang.doc.view.dto.Param;
import com.liuzhihang.doc.view.integration.TornaFacadeService;
import com.liuzhihang.doc.view.integration.impl.TornaFacadeServiceImpl;
import com.liuzhihang.doc.view.notification.DocViewNotification;
import com.liuzhihang.doc.view.service.DocViewUploadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 上传到 Torna
 * <p>
 * 将 docViewMap 转换为 torna 对象并调用 YApiFacadeService
 *
 * @author liuzhihang
 * @date 2021/6/8 23:57
 */
@Slf4j
@Service
public final class TornaServiceImpl implements DocViewUploadService {


    @Override
    public boolean checkSettings(@NotNull Project project) {

        YApiSettings apiSettings = YApiSettings.getInstance(project);

        if (StringUtils.isBlank(apiSettings.getUrl())
                || apiSettings.getProjectId() == null
                || StringUtils.isBlank(apiSettings.getToken())) {
            // 说明没有配置 torna 上传地址, 跳转到配置页面
            DocViewNotification.notifyError(project, DocViewBundle.message("notify.torna.info.settings"));
            ShowSettingsUtil.getInstance().showSettingsDialog(project, TornaSettingsConfigurable.class);
            return false;
        }
        return true;
    }

    @Override
    public void doUpload(@NotNull Project project, @NotNull DocView docView) {

        try {
            TornaSettings settings = TornaSettings.getInstance(project);

            TornaFacadeService facadeService = ApplicationManager.getApplication().getService(TornaFacadeServiceImpl.class);



            DocPushRequest request = new DocPushRequest(settings.getToken());
//            request.setDebugEnvs();
//            request.setAuthor();
            request.setCommonErrorCodes(new ArrayList<>());
            request.setIsReplace(Booleans.TRUE);
            request.setIsOverride(Booleans.TRUE);

            // 创建分类
            DocItem folder = new DocItem();
            folder.setIsFolder(Booleans.TRUE);
            folder.setName(docView.getDocTitle());
//            folder.setAuthor("李四");

            List<DocItem> apis = new ArrayList<>();

            DocItem item = new DocItem();
            item.setName(docView.getDocTitle());
            item.setDescription(docView.getName());
//            item.setAuthor();
            item.setUrl(docView.getPath());
//            item.setVersion();
//            item.setDeprecated();
            item.setDefinition(docView.getPsiClass().getQualifiedName());
            item.setHttpMethod(docView.getMethod());
            item.setContentType(docView.getContentType().getValue());
            item.setParentId("");
            item.setIsShow(Booleans.TRUE);
            item.setOrderIndex(1);
//            item.setIsRequestArray();
//            item.setIsResponseArray();
//            item.setRequestArrayType();
//            item.setResponseArrayType();
//            item.setItems();
//            item.setPathParams();
            item.setHeaderParams(buildReqHeaders(docView.getHeaderList()));
            item.setQueryParams(buildReqQuery(docView.getReqParamList()));
            item.setRequestParams(buildReqQuery(docView.getReqParamList()));
            item.setResponseParams(buildJsonSchema(docView.getRespBody().getChildList()));
//            item.setErrorCodeParams();
//            item.setDubboInfo();
            apis.add(item);
            folder.setItems(apis);

            request.setApis(Arrays.asList(folder));
            DebugEnv debugEnv = new DebugEnv("测试环境", "http://localhost:8090");
            request.setDebugEnvs(Arrays.asList(debugEnv));




//            YApiCat cat = getOrAddCat(settings, docView.getDocTitle());
//
//            YapiSave save = new YapiSave();
//            save.setYapiUrl(settings.getUrl());
//            save.setToken(settings.getToken());
//            save.setProjectId(settings.getProjectId());
////            save.setCatId(cat.getId());
//
//            if ("Dubbo".equals(docView.getMethod())) {
//                // dubbo 接口处理
//                save.setPath("/Dubbo/" + docView.getPsiMethod().getName());
//                save.setMethod("POST");
//            } else {
//                save.setMethod(docView.getMethod());
//                save.setPath(docView.getPath());
//            }
//            // 枚举: raw,form,json
//            save.setReqBodyType(docView.getContentType().toString().toLowerCase());
//            save.setReqBodyForm(new ArrayList<>());
//            save.setReqParams(new ArrayList<>());
//            save.setReqHeaders(buildReqHeaders(docView.getHeaderList()));
//            save.setReqQuery(buildReqQuery(docView.getReqParamList()));
//            save.setResBodyType("json");
//            save.setResBody(buildJsonSchema(docView.getRespBody().getChildList()));
//            save.setMarkdown(buildDesc(docView));
//            save.setTitle(docView.getName());
//
//            if (docView.getContentType().equals(ContentTypeEnum.JSON)) {
//                save.setReqBodyIsJsonSchema(true);
//                save.setReqBodyOther(buildJsonSchema(docView.getReqBody().getChildList()));
//            }

            facadeService.save(request, settings.getUrl());

//            String yapiInterfaceUrl = settings.getUrl() + "/project/" + settings.getProjectId() + "/interface/api/cat_" + cat.getId();

            DocViewNotification.uploadSuccess(project, "Torna", settings.getUrl());
        } catch (Exception e) {
            DocViewNotification.notifyError(project, DocViewBundle.message("notify.torna.upload.error", e.getMessage()));
            log.error("上传单个文档失败:{}", docView, e);
        }

    }


    /**
     * JsonSchema 信息如下
     * <p>
     * type: 数据类型 object array
     * required: 必填字段列表
     * title:标题
     * description:描述
     * properties: 字段列表
     * <p>
     * items: 数组类型时内部元素
     */
    private List<DocParamResp> buildJsonSchema(List<Body> bodyList) {
        List<DocParamResp> properties = new ArrayList<>();

        buildProperties(properties, bodyList);

        return properties;
    }

    /**
     * {
     * "type": "String",
     * "mock": {
     * "mock": "@string"
     * }
     * }
     */
    private void buildProperties(List<DocParamResp> properties, List<Body> bodyList) {

        for (Body body : bodyList) {

            DocParamResp innerProperties = new DocParamResp();
            // mock 数据先不填充

            // 设置 body
            if (CollectionUtils.isNotEmpty(body.getChildList()) && body.getPsiElement() instanceof PsiField) {

                PsiField field = (PsiField) body.getPsiElement();
                PsiType type = field.getType();

                if (type instanceof PsiPrimitiveType || FieldTypeConstant.FIELD_TYPE.containsKey(type.getPresentableText())) {
                    // 基础类型
                    innerProperties.setType(body.getType());
                    innerProperties.setRequired(body.getRequired() ? Booleans.TRUE : Booleans.FALSE);
                    innerProperties.setDescription(body.getDesc());
                } else if (InheritanceUtil.isInheritor(type, CommonClassNames.JAVA_UTIL_COLLECTION)) {
                    List<DocParamResp> itermProperties = new ArrayList<>();
                    buildProperties(itermProperties, body.getChildList());

                    innerProperties.setType("array");
                    innerProperties.setRequired(body.getRequired() ? Booleans.TRUE : Booleans.FALSE);
                    innerProperties.setDescription(body.getType());
                    innerProperties.setChildren(itermProperties);

                } else {
                    // InheritanceUtil.isInheritor(type, CommonClassNames.JAVA_UTIL_MAP)
                    // 对象 和 Map
                    List<DocParamResp> objectProperties = new ArrayList<>();

                    buildProperties(objectProperties, body.getChildList());
                    innerProperties.setRequired(body.getRequired() ? Booleans.TRUE : Booleans.FALSE);
                    innerProperties.setDescription(body.getType());
                    innerProperties.setChildren(objectProperties);
                }

            } else if (body.getPsiElement() instanceof PsiClass) {

                if (InheritanceUtil.isInheritor((PsiClass) body.getPsiElement(), CommonClassNames.JAVA_UTIL_COLLECTION)) {
                    // 参数是 List<User>
                    List<DocParamResp> itermProperties = new ArrayList<>();
                    buildProperties(itermProperties, body.getChildList());

                    innerProperties.setType("array");
                    innerProperties.setRequired(body.getRequired() ? Booleans.TRUE : Booleans.FALSE);
                    innerProperties.setDescription(body.getType());
                    innerProperties.setChildren(itermProperties);
                } else {
                    // InheritanceUtil.isInheritor(type, CommonClassNames.JAVA_UTIL_MAP)
                    List<DocParamResp> objectProperties = new ArrayList<>();

                    buildProperties(objectProperties, body.getChildList());
                    innerProperties.setRequired(body.getRequired() ? Booleans.TRUE : Booleans.FALSE);
                    innerProperties.setDescription(body.getType());
                    innerProperties.setChildren(objectProperties);
                }

            } else {
                // 基础类型
                innerProperties.setType(body.getType());
                innerProperties.setRequired(body.getRequired() ? Booleans.TRUE : Booleans.FALSE);
                innerProperties.setDescription(body.getType());
            }
        }

    }


    private List<DocParamReq> buildReqQuery(List<Param> paramList) {

        if (CollectionUtils.isEmpty(paramList)) {
            return new ArrayList<>();
        }

        return paramList.stream().map(param -> {
            DocParamReq apiQuery = new DocParamReq();
            apiQuery.setName(param.getName());
            apiQuery.setType(param.getType());
            apiQuery.setRequired(param.getRequired() ? Booleans.TRUE : Booleans.FALSE);
//            apiQuery.setMaxLength();
            apiQuery.setExample(param.getExample());
            apiQuery.setDescription(param.getDesc());
//            apiQuery.setEnumInfo();
//            apiQuery.setParentId();
//            apiQuery.setOrderIndex();
//            apiQuery.setChildren();
            return apiQuery;
        }).collect(Collectors.toList());

    }

    private List<DocParamHeader> buildReqHeaders(List<Header> headerList) {

        if (CollectionUtils.isEmpty(headerList)) {
            return new ArrayList<>();
        }

        return headerList.stream().map(header -> {
            DocParamHeader apiHeader = new DocParamHeader();
            apiHeader.setName(header.getName());
            apiHeader.setRequired(header.getRequired() ? Booleans.TRUE : Booleans.FALSE);
//            apiHeader.setExample();
            apiHeader.setDescription(header.getDesc());
            return apiHeader;
        }).collect(Collectors.toList());


    }

}
