<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="com.buildfifteen.teamcity.uebuildgraph.BuildGraphParametersProvider"/>

<c:set var="opt_dlg_key_id" value="ue4bg-edit-option-key"/>
<c:set var="opt_dlg_val_id" value="ue4bg-edit-option-value"/>
<c:set var="opt_val_class" value="ue4bg-option-value"/>
<c:set var="opt_key_class" value="ue4bg-option-key" />
<c:set var="opt_table_id" value="ue4bg-options-table" />
<c:set var="opt_table_empty_id" value="ue4bg-options-table-empty-msg" />
<c:set var="opt_table_template_id" value="ue4bg-options-table-template" />

<l:settingsGroup title="UGS Notifications">
    <tr>
        <th><label for="${params.keyUGSEnable}"></label>Enable: </th>
        <td>
            <props:checkboxProperty name="${params.keyUGSEnable}"/>
            <span class="error" id="error_${params.keyUGSEnable}"></span>
            <span class="smallNote">Enable UGS Metadata server Notifications.  You must define a server URL in the parent project for this to work.</span>
        </td>
    </tr>
    <tr id="ugs-notifications-project">
        <th><label for="${params.keyUGSProject}">Project path: <l:star/></label></th>
        <td>
            <div class="posRel">
                <props:textProperty name="${params.keyUGSProject}" className="longField"/>
                <span class="error" id="error_${params.keyUGSProject}"></span>
                <span class="smallNote">Path to .uproject file in Perforce</span>
            </div>
        </td>
    </tr>
    <tr id="ugs-notifications-change" class="advancedSetting">
        <th><label for="${params.keyUGSChange}">Change:</label></th>
        <td>
            <div class="posRel">
                <props:textProperty name="${params.keyUGSChange}" className="longField"/>
                <span class="error" id="error_${params.keyUGSChange}"></span>
                <span class="smallNote">Change number for UGS - leave blank to use the change of the first VCS</span>
            </div>
        </td>
    </tr>
    <tr id="ugs-notifications-badge">
        <th><label for="${params.keyUGSBadge}">Badge Name: <l:star/></label></th>
        <td>
            <div class="posRel">
                <props:textProperty name="${params.keyUGSBadge}" className="longField"/>
                <span class="error" id="error_${params.keyUGSBadge}"></span>
                <span class="smallNote">Badge Name in UGS</span>
            </div>
        </td>
    </tr>
    <tr id="ugs-notifications-nostart">
        <th><label for="${params.keyUGSNoStart}">Skip Notify Start: </label></th>
        <td>
            <props:checkboxProperty name="${params.keyUGSNoStart}"/>
            <span class="error" id="error_${params.keyUGSNoStart}"></span>
            <span class="smallNote">Do not send Start notifications</span>
        </td>
    </tr>
</l:settingsGroup>
<l:settingsGroup title="UE4 Build Graph settings">
    <script>
        BS.BGOptionsTable = {
            _showTable: function() {
                $j('#${opt_table_empty_id}').attr('hidden', 'hidden');
                $j('table#${opt_table_id}').removeAttr('hidden');
            },

            _hideTable: function() {
                $j('#${opt_table_empty_id}').removeAttr('hidden');
                $j('table#${opt_table_id}').attr('hidden', 'hidden');
            },

            addRow: function (el_table) {
                let row = $j('tr#${opt_table_template_id}').clone().prop('id','').removeAttr('hidden');
                row.appendTo(el_table.find('tbody'));
                this._showTable();
                return row;
            },

            editRow: function(el_row, key, val) {
                el_row.find('td.${opt_key_class}').text(key);
                el_row.find('td.${opt_val_class}').text(val);
            },

            removeRow: function(el_row, el_optionsParameter) {
                if (!confirm("Are you sure you want to delete this option?")) return;
                const el_tbody = el_row.closest('tbody');
                el_row.remove();
                this.updateOptionsParameter(el_tbody, el_optionsParameter);
                if( el_tbody.find('tr').length == 0 ) {
                    this._hideTable();
                }
            },

            updateOptionsParameter: function(el_tbody, el_optionsParameter) {
                let options = "";
                el_tbody.find('tr').each( (i, el) => {
                    const key = $j(el).find('td.${opt_key_class}').text().trim();
                    const val = $j(el).find('td.${opt_val_class}').text().trim();
                    if( key != '' && val != '') {
                        options += key + '§' + val + '§';
                    }
                });
                if(options.length > 0) {
                    el_optionsParameter.val(options.substring(0, options.length - 1));
                }
                else {
                    el_optionsParameter.val("");
                }
            },
        }

        BS.EditBGOptionDialog = OO.extend(BS.AbstractModalDialog, {
            row: null,
            tbody: null,
            template: null,

            getContainer: function() {
                return $('editOptionDialog');
            },

            showDialog_EditOption: function(rowEl) {
                this.row = rowEl;
                this.tbody = null;
                this.template = null;

                const name = rowEl.find('td.${opt_key_class}').text();
                const value = rowEl.find('td.${opt_val_class}').text();

                const el_name =  $('${opt_dlg_key_id}');
                const el_value = $('${opt_dlg_val_id}');
                el_name.value = name;
                el_value.value = value;
                $('editOptionFormTitle').innerHTML = "Edit Option";

                this.showCentered();

                Form.Element.enable(el_value);
                Form.Element.disable(el_name);
                el_value.focus();

                this.updateVisibilityHandlers();
            },

            showDialog_NewOption: function(el_tbody, el_template) {
                this.row = null;
                this.tbody = el_tbody;
                this.template = el_template;

                $('${opt_dlg_key_id}').value = "";
                $('${opt_dlg_val_id}').value = "";
                $('editOptionFormTitle').innerHTML = "Add New Option";

                this.showCentered();

                Form.Element.enable($('${opt_dlg_key_id}'));
                Form.Element.enable($('${opt_dlg_val_id}'));
                $('${opt_dlg_key_id}').focus();

                this.updateVisibilityHandlers();
            },

            submitOption : function(el_optionsParameter) {
                const key = $('${opt_dlg_key_id}').value;
                const val = $('${opt_dlg_val_id}').value;
                if(this.row == null) {
                    this.row = BS.BGOptionsTable.addRow(this.tbody, this.template)
                    this.tbody = null;
                    this.template = null;
                }
                BS.BGOptionsTable.editRow(this.row, key, val);
                BS.BGOptionsTable.updateOptionsParameter(this.row.closest('tbody'), el_optionsParameter);
                this.close();
            },

            updateVisibilityHandlers : function() {
                BS.VisibilityHandlers.updateVisibility(this.getContainer());
            },

            cancelDialog: function() {
                this.close();
            },
        });
    </script>
    <bs:dialog dialogId="editOptionDialog"
               title="Set Option"
               titleId="editOptionFormTitle"
               closeCommand="BS.EditBGOptionDialog.cancelDialog(); return false">
        <forms:multipartForm id="editOptionForm"
                             targetIframe="hidden-iframe">
            <div class="posRel">
                <label class="editRequirementLabel" for="${opt_dlg_key_id}">Option Name: <l:star/></label>
                <forms:textField
                        name="${opt_dlg_key_id}"
                        maxlength="1024"
                        style="margin:0; padding:0; width:25em;"
                        noAutoComplete="true"/>
                <bs:projectData type="BuildGraphOptions"
                                sourceFieldId="${params.keyScriptName}"
                                targetFieldId="${opt_dlg_key_id}"
                                popupTitle="Select Option"
                                selectionMode="single" />
                <span class="error" id="error_${opt_dlg_key_id}" style="margin-left: 10.5em;"></span>
            </div>
            <div class="clr spacing"></div>
            <label class="editRequirementLabel" for="${opt_dlg_val_id}">Value: <l:star/></label>
            <forms:textField
                    name="${opt_dlg_val_id}"
                    maxlength="1024"
                    style="margin:0; padding:0; width:25em;"
                    className="textField textProperty" />
            <div class="error" id="error_${opt_dlg_val_id}" style="margin-left: 10.5em;"></div>
            <div class="popupSaveButtonsBlock">
                <forms:cancel onclick="BS.EditBGOptionDialog.cancelDialog(); return false"/>
                <forms:submit onclick="BS.EditBGOptionDialog.submitOption($j('#${params.keyGraphOptions}')); return false" id="editOptionDialogSubmit" label="Save"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>

    <tr class="advancedSetting">
        <th><label for="${params.keyUATPath}">UAT Path: <l:star/></label></th>
        <td>
            <span>
                <props:textProperty name="${params.keyUATPath}" className="longField">
                    <jsp:attribute name="afterTextField">
                        <bs:vcsTree fieldId="${params.keyUATPath}" treeId="${params.keyUATPath}"></bs:vcsTree>
                    </jsp:attribute>
                </props:textProperty>
            </span>
            <span class="smallNote">The (generally relative to the checkout directory) full path to UAT.bat.  Generally leave as default.</span>
            <span class="error" id="error_${params.keyUATPath}"></span>
        </td>
    </tr>
    <tr>
        <th><label for="${params.keyScriptName}">Build Script: <l:star/></label></th>
        <td>
            <span>
                <props:textProperty name="${params.keyScriptName}" className="longField">
                    <jsp:attribute name="afterTextField">
                        <bs:vcsTree fieldId="${params.keyScriptName}" treeId="${params.keyScriptName}"></bs:vcsTree>
                    </jsp:attribute>
                </props:textProperty>
            </span>
            <span class="smallNote">The (generally relative to the checkout directory) name and path to the Build Graph XML file</span>
            <span class="error" id="error_${params.keyScriptName}"></span>
        </td>
    </tr>
    <tr>
        <th><label for="${params.keyNodeName}">Node: <l:star/></label></th>
        <td>
            <div class="posRel">
                <props:textProperty name="${params.keyNodeName}" className="longField"/>
                <bs:projectData type="BuildGraphNodes" sourceFieldId="${params.keyScriptName}"
                                targetFieldId="${params.keyNodeName}" popupTitle="Select Target Node"
                                selectionMode="single" />

                <span class="error" id="error_${params.keyNodeName}"></span>
                <span class="smallNote">The name of the node to build</span>
            </div>
        </td>
    </tr>
    <tr class="advancedSetting">
        <th><label for="${params.keyAddBuildMachine}">Set 'buildmachine' option: </label></th>
        <td>
            <props:checkboxProperty name="${params.keyAddBuildMachine}"/>
            <span class="error" id="error_${params.keyAddBuildMachine}"></span>
            <span class="smallNote">Adds the -buildmachine option to the command line to automatically set Perforce info, versioning etc</span>
        </td>
    </tr>
    <tr class="advancedSetting">
        <th><label for="${params.keyUseP4}">Set NoP4 option</label></th>
        <td>
            <props:checkboxProperty name="${params.keyUseP4}"/>
            <span class="error" id="error_${params.keyUseP4}"></span>
            <span class="smallNote">Adds -NoP4 if ticked</span>
        </td>
    </tr>
    <tr class="advancedSetting">
        <th><label for="${params.keyReformatLog}">Reformat Log</label></th>
        <td>
            <props:checkboxProperty name="${params.keyReformatLog}"/>
            <span class="error" id="error_${params.keyReformatLog}"></span>
            <span class="smallNote">Parses the log to (re-)write more Teamcity friendly messages</span>
        </td>
    </tr>
    <tr class="advancedSetting">
        <th><label for="${params.keyAdditionalParameters}">Additional Parameters:</label></th>
        <td>
            <div class="posRel">
                <props:textProperty name="${params.keyAdditionalParameters}" className="longField"/>
                <span class="error" id="error_${params.keyAdditionalParameters}"></span>
                <span class="smallNote">Additional command line parameters</span>
            </div>
        </td>
    </tr>
    <tr>
        <th><label for="${params.keyGraphOptions}">Options</label></th>
        <td>
            <props:hiddenProperty name="${params.keyGraphOptions}"/>
            <span class="smallNote">Each will be added as '-set:{name}={value}' on the generated command line.  Values that include spaces will put in to quotes.</span>

            <c:set var="options" value="${fn:split(propertiesBean.properties[params.keyGraphOptions],'§')}"/>
            <c:set var="num_options" value="${(fn:length(options) / 2)-1}"/>
            <c:choose>
                <c:when test="${fn:length(options)>1}">
                    <c:set var="options_msg_vis" value="hidden" />
                    <c:set var="options_tbl_vis" value="" />
                </c:when>
                <c:otherwise>
                    <c:set var="options_msg_vis" value="" />
                    <c:set var="options_tbl_vis" value="hidden" />
                </c:otherwise>
            </c:choose>
            <strong id="${opt_table_empty_id}" ${options_msg_vis} >None Defined.</strong>
            <table id="${opt_table_id}" class="highlightable parametersTable" ${options_tbl_vis}>
                <thead>
                <tr style="background-color: #f5f5f5;">
                    <th>Name</th>
                    <th colspan="3">Value</th>
                </tr>
                <tr id="${opt_table_template_id}" hidden>
                    <td class="${opt_key_class} highlight"></td>
                    <td class="${opt_val_class} highlight beforeActions" onclick="BS.EditBGOptionDialog.showDialog_EditOption($j(this).closest('tr'));"></td>
                    <td class="edit highlight">
                        <a href="#" showdiscardchangesmessage="false" onclick="BS.EditBGOptionDialog.showDialog_EditOption($j(this).closest('tr')); Event.stop(event)">Edit</a>
                    </td>
                    <td class="edit highlight">
                        <a href="#" showdiscardchangesmessage="false" onclick="BS.BGOptionsTable.removeRow($j(this.closest('tr')), $j('#${params.keyGraphOptions}')); return false">Delete</a>
                    </td>
                </tr>
                </thead>
                <tbody>
                <c:if test="${fn:length(options)>1}">
                    <c:forEach items="${options}" var="the_name" step="2" varStatus="theCount">
                        <c:set var="the_value" value="${options[theCount.index + 1]}"/>
                        <tr class="ue4bg_option">
                            <td class="${opt_key_class} highlight"><c:out value="${the_name}"/></td>
                            <td class="${opt_val_class} highlight beforeActions" onclick="BS.EditBGOptionDialog.showDialog_EditOption($j(this).closest('tr'));"><c:out value="${the_value}"/></td>
                            <td class="edit highlight">
                                <a href="#" showdiscardchangesmessage="false" onclick="BS.EditBGOptionDialog.showDialog_EditOption($j(this).closest('tr')); Event.stop(event)">Edit</a>
                            </td>
                            <td class="edit highlight">
                                <a href="#" showdiscardchangesmessage="false" onclick="BS.BGOptionsTable.removeRow($j(this.closest('tr')), $j('#${params.keyGraphOptions}')); return false">Delete</a>
                            </td>
                        </tr>
                    </c:forEach>
                </c:if>
                </tbody>
            </table>
            <span class="error" id="error_${params.keyGraphOptions}"></span>
            <forms:addButton title="Add Option" onclick="BS.EditBGOptionDialog.showDialog_NewOption($j('table#${opt_table_id}'))">Add Option</forms:addButton>
        </td>
    </tr>
</l:settingsGroup>