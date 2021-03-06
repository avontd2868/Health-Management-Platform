package org.osehra.cpe.vpr.queryeng;

import org.osehra.cpe.datetime.PointInTime;
import org.osehra.cpe.datetime.format.HL7DateTimeFormat;
import org.osehra.cpe.vpr.Medication;
import org.osehra.cpe.vpr.queryeng.ColDef.ActionColDef;
import org.osehra.cpe.vpr.queryeng.ColDef.HL7DTMColDef;
import org.osehra.cpe.vpr.queryeng.Query.FrameQuery;
import org.osehra.cpe.vpr.queryeng.query.JDSQuery;
import org.osehra.cpe.vpr.queryeng.query.QueryDef;
import org.osehra.cpe.vpr.queryeng.query.QueryDefCriteria;
import org.osehra.cpe.vpr.viewdef.QueryMapper;
import org.osehra.cpe.vpr.viewdef.RenderTask;
import org.osehra.cpe.vpr.ws.link.OpenInfoButtonLinkGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;

@Component(value = "org.osehra.cpe.vpr.queryeng.MedsIVViewDef")
@Scope("prototype")
public class MedsIVViewDef extends ViewDef {
    @Autowired
    public MedsIVViewDef(OpenInfoButtonLinkGenerator linkgen, Environment env) {
        // declare the view parameters
        declareParam(new ViewParam.ViewInfoParam(this, "Medications"));
        declareParam(new ViewParam.PatientIDParam());
        declareParam(new ViewParam.DateRangeParam("range", "2000..NOW"));
        declareParam(new ViewParam.AsArrayListParam("filter.typeCodes"));
//        declareParam(new ViewParam.ENUMParam("filter_kind", "ALL", "O", "I", "N")).addMeta("multiple", true);
        declareParam(new ViewParam.AsArrayListParam("filter_kind"));
        declareParam(new ViewParam.ENUMParam("filter_status", null, "ACTIVE", "PENDING", "DISCONTINUED", "EXPIRED").addMeta("multiple", true).addMeta("title", "Status filter"));
        declareParam(new ViewParam.AsArrayListParam("filter_status"));
        declareParam(new ViewParam.AsArrayListParam("filter_class"));
        declareParam(new ViewParam.QuickFilterParam("qfilter_status", "", "ACTIVE", "PENDING", "DISCONTINUED", "EXPIRED"));
        declareParam(new ViewParam.AsArrayListParam("qfilter_status"));
        declareParam(new ViewParam.SortParam("overallStart", false));

        // list of fields that are not displayable as columns and a default user column set/order
        String displayCols = "rowactions,name,dose,sig,doseChange,lastDoseChange,lastDose,vaStatus,renewBy,overallStart,overallStop,facility";
        String requireCols = "rowactions,facility,overallStart,overallStop";
        String hideCols = "uid,pid,selfLink,history";
        String sortCols = "overallStart,kind,overallStop,medStatusName,vaStatus";
        String groupCols = "vaStatus,medStatusName,ingredientName,drugClassName";
//        String groupCols = "kind";
        declareParam(new ViewParam.ColumnsParam(this, displayCols, requireCols, hideCols, sortCols, groupCols));

        QueryDef qry = new QueryDef();
        qry.addCriteria(QueryDefCriteria.where("vaType").in("?:filter_kind"));
        
        /*
		 * The following code is in place just so we can merge two different "in" criterium 
		 * correctly. The current QueryDefCriteria mechanisms can't handle this for the same key value (column)
		 */
		qry.addCriteria(QueryDefCriteria.where("vaStatus").in(new QueryDefCriteria.SpelRef(){
			@Override
			public boolean filterOut(Map<String, Object> params) {
				return false;
			}
			@Override
			public Object evaluateWithParams(Map<String, Object> params) {
				Object qcoll = params.get("qfilter_status");
				Object fcoll = params.get("filter_status");
				Vector<String> rslt = new Vector<String>();
				String[] filter = {"vaStatus","ACTIVE", "PENDING", "DISCONTINUED", "EXPIRED"};
				for(int i = 1; i<filter.length; i++)
				{
					if(qcoll instanceof List && qcoll!=null && ((List<String>)qcoll).size()>0 && !((List<String>)qcoll).contains(filter[i]))
					{
						continue;
					}
					if(fcoll instanceof List && fcoll!=null && ((List<String>)fcoll).size()>0 && !((List<String>)fcoll).contains(filter[i]))
					{
						continue;
					}
					rslt.add(filter[i]);
				}
				return rslt;
			}
		}));
        
//        qry.addCriteria(QueryDefCriteria.where("products[].drugClassName").in("?:filter_class"));

//        Query q1 = new JDSQuery("uid", qry, "vpr/{pid}/index/medication?order=#{getParamStr('sort.ORDER_BY')}") {
        Query q1 = new JDSQuery("uid", qry, "vpr/{pid}/index/med-qualified-name/") {
            @Override
            public void exec(RenderTask task) throws Exception {
                super.exec(task);
                ArrayList<Map<String, Object>> tmp = new ArrayList<Map<String, Object>>();
//                Map<String, Object> doseMap = new HashMap<String, Object>();
                Map<String, ArrayList> medNameList = new HashMap<String, ArrayList>();
                tmp.addAll(task);
                task.removeAll(tmp);

                String medName;
                String uid;
                for (int i = 0; i < tmp.size(); i++) {
                    Map<String, Object> row = new HashMap<String, Object>();
                    row = tmp.get(i);
                    Map<String, String> value = new HashMap<String, String>();
                    String vaType = row.get("vaType").toString();
                    if (!vaType.equals("V")) continue;

                    Map<String, Object> rowData = new HashMap<String, Object>();
                    rowData = getIvName(row);
                    String sig = getIvSig(row);
                    rowData.put("uid", row.get("uid"));
                    rowData.put("facility", row.get("facilityName"));
                    rowData.put("vaType", vaType);
                    rowData.put("kind", row.get("kind").toString());
                    rowData.put("medStatusName", row.get("medStatusName").toString());
                    rowData.put("sig", rowData.get("addDose").toString() + " " + sig);
                    rowData.put("vaStatus", row.get("vaStatus").toString());
                    rowData.put("lastAdministered", "needs BCMA");
                    PointInTime start = null;
                    PointInTime stop = null;
                    if (row.containsKey("overallStart")) start = HL7DateTimeFormat.parse(row.get("overallStart").toString());
                    if (row.containsKey("stopped")) stop = HL7DateTimeFormat.parse(row.get("stopped").toString());
                    rowData.put("overallStart", start);
                    rowData.put("overallStop", stop);
                    rowData.put("lastDoseChange", "TBD");
                    rowData.put("totalDose", "TBD");
                    task.add(rowData);

            }
            }

            private Map<String, Object> getIvName(Map<String, Object> data) {
                Map<String, Object> result = new HashMap<String, Object>();
                String solution = "";
                String additive = "";
                String addDose = "";
                String solDose = "";
                ArrayList<Map<String, Object>> productsList = new ArrayList<Map<String, Object>>();
                productsList = (ArrayList) data.get("products");
                for (int p = 0; p < productsList.size(); p++) {
                    Map<String, Object> product = new HashMap<String, Object>();
                    product.putAll(productsList.get(p));
                    String temp;
                    String name;
                    String role;
                    if ((product.containsKey("ingredientRole"))&& (product.containsKey("ingredientName"))) {
                        role = product.get("ingredientRole").toString();
                        name = product.get("ingredientName").toString();
                        if (role.equals("urn:sct:418804003")) {
                            if (additive.equals("")) additive = name;
                            else additive = additive + ',' + name;
                            if (product.containsKey("strength")) {
                                temp = product.get("strength").toString();
                                if (addDose.equals("")) addDose = temp;
                                else addDose = addDose + ',' + temp;
                            }
                        } else {
                            if (solution.equals("")) solution = name;
                            else solution = solution + ',' + name;
                            if (product.containsKey("volume")) {
                                temp = product.get("volume").toString();
                                if (solDose.equals("")) solDose = temp;
                                else solDose = solDose + ',' + temp;
                            }
                        }

                    }
                }
                result.put("name",  additive + " - " + solution);
                result.put("addDose", addDose);
                result.put("dose", addDose + " in " + solDose);
                return result;
            }

            private String getIvSig(Map <String, Object> data) {
            String result = "";
            ArrayList<Map<String, Object>> dosagesList = new ArrayList<Map<String, Object>>();
            dosagesList = (ArrayList) data.get("dosages");
            Map <String, Object> dose = new HashMap<String, Object>();
            dose.putAll(dosagesList.get(0));
            if (!dose.containsKey("routeName")) return result;
            result = dose.get("routeName").toString();
            if (dose.containsKey("scheduleName")) result = result + " " + dose.get("scheduleName").toString();
            if (dose.containsKey("duration")) result = result + " " + dose.get("duration").toString();
            if (dose.containsKey("restriction")) result = result + " for a total of " + dose.get("restriction").toString();
            return result;
            }
        };

        addQuery(q1);
//        addColumns(q1, "uid", "pid", "summary", "overallStart", "overallStop", "facility", "kind", "vaStatus", "medStatusName", "ingredientName", "drugClassName");
        addColumns(q1, "uid", "pid", "name", "dose", "sig", "overallStart", "overallStop", "facility", "kind", "vaStatus", "medStatusName", "ingredientName", "drugClassName");

        getColumn("name").setMetaData("text", "Name");
        getColumn("name").setMetaData("minWidth", 200);
        getColumn("name").setMetaData("flex", 1);
        addColumn(new HL7DTMColDef(q1, "overallStart"));
        getColumn("overallStart").setMetaData("text", "Start Date").setMetaData("width", 75);

        addColumn(new HL7DTMColDef(q1, "overallStop")).setMetaData("detailfield", "infobtnurl");
        getColumn("overallStop").setMetaData("text", "Stop Date").setMetaData("width", 75);


        getColumn("dose").setMetaData("text", "Dose");
        getColumn("sig").setMetaData("text", "Sig");
//        addColumn(new HL7DTMColDef(q1, "lastDoseChange"));
//        getColumn("lastDoseChange").setMetaData("text", "Last Dose Change");
//        getColumn("lastDose").setMetaData("text", "Last Dose");
//        getColumn("renewBy").setMetaData("text", "Renew By");
//        getColumn("dose").setMetaData("text", "Dose");
        getColumn("vaStatus").setMetaData("text", "VA Status");
        getColumn("medStatusName").setMetaData("text", "HITSP Status");
        getColumn("facility").setMetaData("text", "Facility");

        addQuery(new QueryMapper.PerRowAppendMapper(new Query.InfobuttonQuery("infobtnurl", "[TBD]", "ingredientName", "MLREV", "2.16.840.1.113883.6.88")));
        addQuery(new QueryMapper.PerRowAppendMapper(new FrameQuery("uid", "viewdefactions", Medication.class)));
        addColumn(new DomainClassSelfLinkColDef("selfLink", Medication.class)).setMetaData("detailloader", "html");
        addColumn(new ActionColDef("rowactions"));

    }
}

