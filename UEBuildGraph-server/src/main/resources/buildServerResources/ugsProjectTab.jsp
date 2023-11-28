<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<bs:linkCSS>${teamcityPluginResourcesPath}_ugsProjectTab.css</bs:linkCSS>

<div class="section noMargin">
    <h2 class="noBorder">Unreal Game Sync Metadata Server</h2>
    <bs:smallNote>
    This will be used by the Unreal Build Graph Runner to notify the metadata server of start/success/failure of builds.  Specify the Badge and events in the Runner step.
    </bs:smallNote>

    <c:if test="${fn:length(parentServerUrl)>1}">
        <div>
            <label class="editRequirementLabel" for="parentServerUrl">Current Server URL (from parent): </label>
            <forms:textField name="parentServerUrl" value="${parentServerUrl}" className="longField" style="font-style:italic;" readonly="true" />
        </div>
    </c:if>

    <form id="ugsserver" action="<c:url value='${ugsSettingsEndpoint}'/>" method="post">
        <div>
            <label class="editRequirementLabel" for="serverUrl">Server URL (for this and child projects) : </label>
            <forms:textField name="serverUrl" value="${serverUrl}" className="longField" />
        </div>
        <input type="hidden" name="projectId" value="${projectId}">
        <forms:submit label="Apply"/>
    </form>
</div>
