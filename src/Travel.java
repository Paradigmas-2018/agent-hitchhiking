import java.util.Date;

public class Travel {
	
	private String destinationCity;
	private String actualCity;
	private Integer quantitySeat;

	
	public Travel(String destinationCity, String actualCity, Integer quantitySeat) {
		super();
		this.destinationCity = destinationCity;
		this.actualCity = actualCity;
		this.quantitySeat = quantitySeat;
	}
	public String getDestinationCity() {
		return destinationCity;
	}
	public void setDestinationCity(String destinationCity) {
		this.destinationCity = destinationCity;
	}
	public String getActualCity() {
		return actualCity;
	}
	public void setActualCity(String actualCity) {
		this.actualCity = actualCity;
	}
	public Integer getQuantitySeat() {
		return quantitySeat;
	}
	public void setQuantitySeat(Integer quantitySeat) {
		this.quantitySeat = quantitySeat;
	}
	
	@Override
	public String toString() {
		return "Travel [destinationCity=" + destinationCity + ", actualCity=" + actualCity + ", quantitySeat="
				+ quantitySeat + "]";
	}
		
}
