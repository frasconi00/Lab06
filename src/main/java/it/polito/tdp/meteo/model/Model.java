package it.polito.tdp.meteo.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import it.polito.tdp.meteo.DAO.MeteoDAO;

public class Model {
	
	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;
	private int costoMinimo;
	private List<Rilevamento> migliore;
	private int costoAttuale;
	private int contaGenova;
	private int contaMilano;
	private int contaTorino;
	MeteoDAO dao;

	public Model() {
		dao = new MeteoDAO();
	}

	// of course you can change the String output with what you think works best
	public String getUmiditaMedia(int mese) {
		
		String result="";
		List<Rilevamento> lista = dao.getAllRilevamentiLocalitaMese(mese, "Genova");
		double media=0;
		if(lista!=null) {
			for(Rilevamento r : lista) {
				media+=r.getUmidita();
			}
			media/=lista.size();
		}
		result+="Umidita media a Genova: "+(int)media+"%\n";
		lista = dao.getAllRilevamentiLocalitaMese(mese, "Milano");
		 media=0;
		if(lista!=null) {
			for(Rilevamento r : lista) {
				media+=r.getUmidita();
			}
			media/=lista.size();
		}
		result+="Umidita media a Milano: "+(int)media+"%\n";
		lista = dao.getAllRilevamentiLocalitaMese(mese, "Torino");
		media=0;
		if(lista!=null) {
			for(Rilevamento r : lista) {
				media+=r.getUmidita();
			}
			media/=lista.size();
		}
		result+="Umidita media a Torino: "+(int)media+"%\n";
		
		return result;
	}
	
	// of course you can change the String output with what you think works best
	public String trovaSequenza(int mese) { //questa prepara la ricorsione
		
		List<Rilevamento> rilevamentiGenova = dao.getAllRilevamentiLocalitaMese(mese, "Genova");
		List<Rilevamento> rilevamentiMilano = dao.getAllRilevamentiLocalitaMese(mese, "Milano");
		List<Rilevamento> rilevamentiTorino = dao.getAllRilevamentiLocalitaMese(mese, "Torino");
		
		List<Rilevamento> parziale = new ArrayList<Rilevamento>();
		List<String> citta = new ArrayList<>();
		citta.add("Genova");
		citta.add("Milano");
		citta.add("Torino");
		costoAttuale=0;
		costoMinimo=Integer.MAX_VALUE;
		contaGenova=0;
		contaMilano=0;
		contaTorino=0;
		
		cerca(parziale,0,rilevamentiGenova,rilevamentiMilano,rilevamentiTorino,citta);
		
		String result = migliore.toString()+"\n\nCosto minimo: "+costoMinimo;
		
		return result;
	}
	
	public void cerca(List<Rilevamento> parziale, int livello, List<Rilevamento> rilevamentiGenova,
			List<Rilevamento> rilevamentiMilano, List<Rilevamento> rilevamentiTorino, List<String> citta) {
		
		if(parziale.size()>=2) { // ci sono almeno due città in parziale?
			//ho cambiato città?-->
			if(!parziale.get(parziale.size()-1).getLocalita().equals(parziale.get(parziale.size()-2).getLocalita())) {
				//ho cambiato città: potevo farlo?
				if(!controllaConsecutivi(parziale)) { //no --> non esplorare ulteriormente
					return;
				}
			}
			
		}
		
		if(contaGenova>NUMERO_GIORNI_CITTA_MAX || contaMilano>NUMERO_GIORNI_CITTA_MAX || contaTorino>NUMERO_GIORNI_CITTA_MAX) {
			//non è una soluzione valida
			return; // non espandere ulteriormente l'albero
		}
		
		if(livello==NUMERO_GIORNI_TOTALI) { //soluzione valida. è migliore di quella attuale?
			//qui bisogna aggiungere a costoAttuale il costo di cambiare città
			//ma non metterlo dentro costoAttuale o devi fare backtracking
			int costoCambio=0;
			//funzione che calcoli i costi di cambio città
			costoCambio=costoCambioCitta(parziale);
			if(costoAttuale+costoCambio<costoMinimo && contaGenova>0 && contaMilano>0 && contaTorino>0) {
				costoMinimo=costoAttuale+costoCambio;
				migliore = new LinkedList<Rilevamento>(parziale);
			}
			
			return; // non espandere ulteriormente l'albero
			
		}
		
		for(String s : citta) {
			if(s.equals("Genova")) {
				
				parziale.add(rilevamentiGenova.get(livello));
				costoAttuale+=parziale.get(parziale.size()-1).getUmidita();
				contaGenova++;
				cerca(parziale,livello+1,rilevamentiGenova,rilevamentiMilano,rilevamentiTorino,citta);
				//backtracking
				costoAttuale= costoAttuale-parziale.get(parziale.size()-1).getUmidita();
				parziale.remove(parziale.size()-1);
				contaGenova--;
				
			}else if(s.equals("Milano")) {
				
				parziale.add(rilevamentiMilano.get(livello));
				costoAttuale+=parziale.get(parziale.size()-1).getUmidita();
				contaMilano++;
				cerca(parziale,livello+1,rilevamentiGenova,rilevamentiMilano,rilevamentiTorino,citta);
				costoAttuale=costoAttuale-parziale.get(parziale.size()-1).getUmidita();
				parziale.remove(parziale.size()-1);
				contaMilano--;
				
			}else { //Torino
				
				parziale.add(rilevamentiTorino.get(livello));
				costoAttuale+=parziale.get(parziale.size()-1).getUmidita();
				contaTorino++;
				cerca(parziale,livello+1,rilevamentiGenova,rilevamentiMilano,rilevamentiTorino,citta);
				costoAttuale=costoAttuale-parziale.get(parziale.size()-1).getUmidita();
				parziale.remove(parziale.size()-1);
				contaTorino--;
				
			}
		}
	
	}
	
	/*
	public boolean controllaConsecutivi(List<Rilevamento> parziale) { 
		
		//il tecnico ha cambiato citta-->controllare che abbia fatto almeno 3 gg consecutivi prima
		if(parziale.size()<4)
			return false;
		
		String penultima = parziale.get(parziale.size()-2).getLocalita();
		String terzultima = parziale.get(parziale.size()-3).getLocalita();
		String quartultima = parziale.get(parziale.size()-4).getLocalita();
		
		if(terzultima!=null && quartultima!=null) {
			if(penultima.equals(terzultima) && terzultima.equals(quartultima)) {
				return true;
			}
		}
		return false;
	}
	*/
	
	private boolean controllaConsecutivi(List<Rilevamento> parziale) { 
		
		//il tecnico ha cambiato citta-->controllare che abbia fatto almeno 3 gg consecutivi prima
		if(parziale.size()<4)
			return false;
		
		for(int i=parziale.size()-2;i>parziale.size()-1-NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN;i--) {
			if(!parziale.get(i).getLocalita().equals(parziale.get(i-1).getLocalita()))
				return false;
		}
		
		return true;
	}
	
	private int costoCambioCitta(List<Rilevamento> parziale) {
		
		int costoCambio=0;
		
		for(int i=1;i<parziale.size();i++)
			if(!parziale.get(i).getLocalita().equals(parziale.get(i-1).getLocalita()))
				costoCambio+=COST;
		
		return costoCambio;
		
	}
	

}
