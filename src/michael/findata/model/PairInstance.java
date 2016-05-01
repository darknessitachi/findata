package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "pair_instance")
@Access(AccessType.FIELD)
public class PairInstance {

	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	private int id;

	@Basic
	@Column(name = "status",columnDefinition="char(6)")
	private String status;

	@Basic
	@Column(name = "date_opened")
	private Timestamp dateOpened;

	@Basic
	@Column(name = "date_closed")
	private Timestamp dateClose;

	@Basic
	@Column(name = "short_open")
	private Double shortOpen;

	@Basic
	@Column(name = "long_open")
	private Double longOpen;

	@Basic
	@Column(name = "threshold_open")
	private Double thresholdOpen;

	@Basic
	@Column(name = "max_amount_possible_open")
	private Double maxAmountPossibleOpen;

	@Basic
	@Column(name = "short_close")
	private Double shortClose;

	@Basic
	@Column(name = "long_close")
	private Double longCLose;

	@Basic
	@Column(name = "threshold_close")
	private Double thresholdClose;

	@Basic
	@Column(name = "max_amount_possible_close")
	private Double maxAmountPossibleClose;

	@Basic
	@Column(name = "short_volume_held")
	private Integer shortVolumeHeld;

	@Basic
	@Column(name = "long_volume_held")
	private Integer longVolumeHeld;

	@Basic
	@Column(name = "min_res")
	private Double minResidule;

	@Basic
	@Column(name = "max_res")
	private Double maxResidule;

	@Basic
	@Column(name = "min_res_date")
	private Timestamp minResiduleDate;

	@Basic
	@Column(name = "max_res_date")
	private Timestamp maxResiduleDate;

	@Basic
	@Column(name = "res_open")
	private Double residuleOpen;

	@Basic
	@Column(name = "res_close")
	private Double residuleClose;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Timestamp getDateOpened() {
		return dateOpened;
	}

	public void setDateOpened(Timestamp dateOpened) {
		this.dateOpened = dateOpened;
	}

	public Timestamp getDateClose() {
		return dateClose;
	}

	public void setDateClose(Timestamp dateClose) {
		this.dateClose = dateClose;
	}

	public Double getShortOpen() {
		return shortOpen;
	}

	public void setShortOpen(Double shortOpen) {
		this.shortOpen = shortOpen;
	}

	public Double getLongOpen() {
		return longOpen;
	}

	public void setLongOpen(Double longOpen) {
		this.longOpen = longOpen;
	}

	public Double getThresholdOpen() {
		return thresholdOpen;
	}

	public void setThresholdOpen(Double thresholdOpen) {
		this.thresholdOpen = thresholdOpen;
	}

	public Double getMaxAmountPossibleOpen() {
		return maxAmountPossibleOpen;
	}

	public void setMaxAmountPossibleOpen(Double maxAmountPossibleOpen) {
		this.maxAmountPossibleOpen = maxAmountPossibleOpen;
	}

	public Double getShortClose() {
		return shortClose;
	}

	public void setShortClose(Double shortClose) {
		this.shortClose = shortClose;
	}

	public Double getLongCLose() {
		return longCLose;
	}

	public void setLongCLose(Double longCLose) {
		this.longCLose = longCLose;
	}

	public Double getThresholdClose() {
		return thresholdClose;
	}

	public void setThresholdClose(Double thresholdClose) {
		this.thresholdClose = thresholdClose;
	}

	public Double getMaxAmountPossibleClose() {
		return maxAmountPossibleClose;
	}

	public void setMaxAmountPossibleClose(Double maxAmountPossibleClose) {
		this.maxAmountPossibleClose = maxAmountPossibleClose;
	}

	public Integer getShortVolumeHeld() {
		return shortVolumeHeld;
	}

	public void setShortVolumeHeld(Integer shortVolumeHeld) {
		this.shortVolumeHeld = shortVolumeHeld;
	}

	public Integer getLongVolumeHeld() {
		return longVolumeHeld;
	}

	public void setLongVolumeHeld(Integer longVolumeHeld) {
		this.longVolumeHeld = longVolumeHeld;
	}

	public Double getMinResidule() {
		return minResidule;
	}

	public void setMinResidule(Double minResidule) {
		this.minResidule = minResidule;
	}

	public Double getMaxResidule() {
		return maxResidule;
	}

	public void setMaxResidule(Double maxResidule) {
		this.maxResidule = maxResidule;
	}

	public Timestamp getMinResiduleDate() {
		return minResiduleDate;
	}

	public void setMinResiduleDate(Timestamp minResiduleDate) {
		this.minResiduleDate = minResiduleDate;
	}

	public Timestamp getMaxResiduleDate() {
		return maxResiduleDate;
	}

	public void setMaxResiduleDate(Timestamp maxResiduleDate) {
		this.maxResiduleDate = maxResiduleDate;
	}

	public Double getResiduleOpen() {
		return residuleOpen;
	}

	public void setResiduleOpen(Double residuleOpen) {
		this.residuleOpen = residuleOpen;
	}

	public Double getResiduleClose() {
		return residuleClose;
	}

	public void setResiduleClose(Double residuleClose) {
		this.residuleClose = residuleClose;
	}
}