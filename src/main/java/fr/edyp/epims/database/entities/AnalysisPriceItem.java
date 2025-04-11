package fr.edyp.epims.database.entities;

import javax.persistence.*;

@Entity
@Table(name = "analysis_price_item", schema = "public")
public class AnalysisPriceItem {

    private int id;

    private float price;
    private String label;

    public AnalysisPriceItem() {
    }

    public AnalysisPriceItem(float price, String label) {
        this.price = price;
        this.label = label;
    }

    public AnalysisPriceItem(int id, float price, String label) {
        this.id = id;
        this.price = price;
        this.label = label;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "analysis_price_item_generator")
    @SequenceGenerator(name = "analysis_price_item_generator", sequenceName = "analysis_price_item_generator_id_seq", allocationSize = 1)
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }


    @Column(name = "price")
    public float getPrice() {
        return this.price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @Column(name = "label")
    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }


}
