package fr.edyp.epims.database.entities;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "analysis_request", schema = "public")
public class AnalysisRequest {

    private int id;
    private int proAnalysisId;
    private int priceListId;
    private String studyRef;
    private Date saveDate;
    private Date exportDate;
    private String jsonData;


    public AnalysisRequest() {
    }

    public AnalysisRequest(int id, int proAnalysisId, int priceListId, String studyRef, Date saveDate, Date exportDate, String jsonData) {
        this.id = id;
        this.proAnalysisId = proAnalysisId;
        this.priceListId = priceListId;
        this.studyRef = studyRef;
        this.saveDate = saveDate;
        this.exportDate = exportDate;
        this.jsonData = jsonData;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "analysis_request_generator")
    @SequenceGenerator(name = "analysis_request_generator", sequenceName = "analysis_request_id_seq", allocationSize = 1)
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "save_date")
    public Date getSaveDate() {
        return this.saveDate;
    }

    public void setSaveDate(Date saveDate) {
        this.saveDate = saveDate;
    }

    @Column(name = "export_date")
    public Date getExportDate() {
        return this.exportDate;
    }

    public void setExportDate(Date exportDate) {
        this.exportDate = exportDate;
    }

    @Column(name = "pro_analysis_id", unique = true, nullable = false)
    public int getProAnalysisId() {
        return this.proAnalysisId;
    }

    public void setProAnalysisId(int proAnalysisId) {
        this.proAnalysisId = proAnalysisId;
    }

    @Column(name = "price_list_id", nullable = false)
    public int getPriceListId() {
        return this.priceListId;
    }

    public void setPriceListId(int priceListId) {
        this.priceListId = priceListId;
    }

    @Column(name = "json_data", columnDefinition="TEXT")
    public String getJsonData() {
        return this.jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    @Column(name = "study_ref")
    public String getStudyRef() {
        return this.studyRef;
    }

    public void setStudyRef(String studyRef) {
        this.studyRef = studyRef;
    }
}
