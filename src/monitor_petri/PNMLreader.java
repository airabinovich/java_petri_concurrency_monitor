package monitor_petri;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import monitor_petri.Arco;
import monitor_petri.Etiqueta;
import monitor_petri.Plaza;
import monitor_petri.Transicion;

import org.javatuples.Triplet;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PNMLreader{
	
	private static final String PLACES = "places";
	private static final String TRANSITIONS = "transitions";
	private static final String ARCS = "arcs";
	
	private Plaza[] plazas; 
	private Transicion[] transiciones;
	private Arco[] arcos;
	File pnmlFile;
	
	public PNMLreader(String pnmlPath) throws FileNotFoundException, SecurityException{
		pnmlFile = new File(pnmlPath);
		
		if(!pnmlFile.exists()){
			throw new FileNotFoundException("File " + pnmlPath + " not found");
		}
		else if(!pnmlFile.canRead()){
			throw new SecurityException("Security Error while trying to read from file " + pnmlPath);
		}
	}
	
	public Triplet<Plaza[], Transicion[], Arco[]> read_file(){
		try {
			Triplet<Plaza[], Transicion[], Arco[]> ret = null;
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(pnmlFile);
					
			// normalize converts 
			// 		<foo>hello 
			// 		wor
			// 		ld</foo>
			// to
			// 		<foo>hello world</foo>
			// to ease parsing
			doc.getDocumentElement().normalize();
			
			// gets every net in the file. We'll only get the first one
			NodeList nets = doc.getElementsByTagName("net");
		
			if (nets.getLength() > 0){
		
				Node net = nets.item(0);
				
				if (net.getNodeType() == Node.ELEMENT_NODE) {
					NodeList netChildren = net.getChildNodes();
					NodeList netElements = null;
					for(int index = 0; index < netChildren.getLength(); index++){
						if(netChildren.item(index).getNodeName().equals("page")){
							netElements = netChildren.item(index).getChildNodes();
						}
					}
					ret = getPetriObjectsFromNodeList(netElements);
				}
			}
			
			return ret;
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	return null;
	    }
	}
	
	private Triplet<Plaza[], Transicion[], Arco[]> getPetriObjectsFromNodeList(NodeList netElements){
		ArrayList<Plaza> places = new ArrayList<Plaza>();
		ArrayList<Transicion> transitions = new ArrayList<Transicion>();
		ArrayList<Arco> arcs = new ArrayList<Arco>();
		for(int index = 0; index < netElements.getLength(); index++){
			Node child = netElements.item(index);
			if(child.getNodeType() == Node.ELEMENT_NODE ){
				NodeList nl = child.getChildNodes();
				String id = ((Element)(child)).getAttribute("id");
				//child tiene el elemento que necesitamos (plaza, transicion o arco)
				System.out.println(child.getNodeName());
				if(child.getNodeName().equals(PLACES)){
					places.add(getPlace(id, child, nl));
				}
				else if(child.getNodeName().equals(TRANSITIONS)){
					transitions.add(getTransition(id, child, nl));
				}
				else if(child.getNodeName().equals(ARCS)){
					arcs.add(getArc(id, child));
				}
			}
		}
		return new Triplet<Plaza[], Transicion[], Arco[]>(
				places.toArray(this.plazas), transitions.toArray(this.transiciones), arcs.toArray(this.arcos));
	}
	
	private Plaza getPlace(String id, Node placeNode, NodeList nl){
		Integer m_inicial = 0;
		for(int i=0; i<nl.getLength(); i++){
			if(nl.item(i).getNodeName() == "initialMarking"){
				System.out.println(nl.item(i).getNodeName());
				m_inicial = Integer.parseInt(nl.item(i).getTextContent().trim());
				return new Plaza(id, m_inicial);
			}
		}
		return null;
	}
	
	private Transicion getTransition(String id, Node transitionNode, NodeList nl){
		
		final String labelRegexString = "[A-Z]";
		final Pattern labelRegex = Pattern.compile(labelRegexString, Pattern.CASE_INSENSITIVE);
		//Una transicion es NO automatica y NO informa, a menos que se diga lo contrario
		boolean isAutomatic = false, isInformed = false;
		for(int i=0; i<nl.getLength(); i++){
			if(nl.item(i).getNodeName().equals("label")){
				String transitionLabels = nl.item(i).getTextContent().trim();
				Matcher labelMatcher = labelRegex.matcher(transitionLabels);
				while(labelMatcher.find()){
					String label = labelMatcher.group();
					isAutomatic = label.equalsIgnoreCase("A") ? true : isAutomatic;
					isInformed = label.equalsIgnoreCase("I") ? true : isInformed;
				}
				return new Transicion(id, new Etiqueta(isAutomatic, isInformed));
			}
		}
		return null;
	}
	
	private Arco getArc(String id, Node arcNode){
		if(arcNode == null){
			return null;
		}
		Element arcElement = (Element)arcNode;
		return new Arco(id, arcElement.getAttribute("source"), arcElement.getAttribute("target"));
	}
}