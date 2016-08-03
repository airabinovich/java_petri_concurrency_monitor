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
	
	private static final String ID = "id";
	private static final String PAGE = "page";
	private static final String NAME = "name";
	private static final String NET = "net";
			
	private static final String PLACE = "place";
	private static final String INITIAL_MARKING = "initialMarking";
	
	private static final String TRANSITION = "transition";
	private static final String LABEL = "label";
	
	private static final String ARC = "arc";
	private static final String WEIGHT = "inscription";
	private static final String SOURCE = "source";
	private static final String TARGET = "target";
	
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
	
	/**
	 * parses PNML file and returns all petri objects embedded
	 * @return a Triplet containing places, transitions and arcs inside the PNML
	 */
	public Triplet<Plaza[], Transicion[], Arco[]> parseFileAndGetPetriObjects(){
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
			NodeList nets = doc.getElementsByTagName(NET);
		
			if (nets.getLength() > 0){
		
				Node net = nets.item(0);
				
				if (net.getNodeType() == Node.ELEMENT_NODE) {
					NodeList netChildren = net.getChildNodes();
					NodeList netElements = null;
					for(int index = 0; index < netChildren.getLength(); index++){
						if(netChildren.item(index).getNodeName().equals(PAGE)){
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
	
	/**
	 * Parses a list of nodes and returns information about places, transitions and arcs embedded
	 * @param netElements a list of nodes children of page node
	 * @return a Triplet containing all places, transitions and arcs inside netElements
	 */
	private Triplet<Plaza[], Transicion[], Arco[]> getPetriObjectsFromNodeList(NodeList netElements){
		ArrayList<Plaza> places = new ArrayList<Plaza>();
		ArrayList<Transicion> transitions = new ArrayList<Transicion>();
		ArrayList<Arco> arcs = new ArrayList<Arco>();
		for(int index = 0; index < netElements.getLength(); index++){
			Node child = netElements.item(index);
			if(child.getNodeType() == Node.ELEMENT_NODE ){
				NodeList nl = child.getChildNodes();
				String id = ((Element)(child)).getAttribute(ID);
				//child tiene el elemento que necesitamos (plaza, transicion o arco)
				if(child.getNodeName().equals(PLACE)){
					places.add(getPlace(id, child, nl));
				}
				else if(child.getNodeName().equals(TRANSITION)){
					transitions.add(getTransition(id, child, nl));
				}
				else if(child.getNodeName().equals(ARC)){
					arcs.add(getArc(id, child, nl));
				}
			}
		}
		
		//sort places and transitions using their indexes
		places.sort((Plaza p1, Plaza p2) -> (p1.getIndice() - p2.getIndice()));
		transitions.sort((Transicion t1, Transicion t2) -> (t1.getIndice() - t2.getIndice()));
		
		Plaza[] retPlaces = new Plaza[places.size()];
		Transicion[] retTransitions = new Transicion[transitions.size()];
		Arco[] retArcs = new Arco[arcs.size()];

		retPlaces = places.toArray(retPlaces);
		retTransitions = transitions.toArray(retTransitions);
		retArcs = arcs.toArray(retArcs);
		
		return new Triplet<Plaza[], Transicion[], Arco[]>(retPlaces, retTransitions, retArcs);
	}
	
	/**
	 * Parses a node and returns the containing place as an object
	 * @param id the object id embedded in the PNML
	 * @param placeNode Node object from PNML
	 * @param nl placeNode children nodes as NodeList
	 * @return
	 */
	private Plaza getPlace(String id, Node placeNode, NodeList nl){
		Integer m_inicial = 0;
		Integer placeIndex = null;
		for(int i=0; i<nl.getLength(); i++){
			String currentNodeName = nl.item(i).getNodeName();
			if(currentNodeName.equals(INITIAL_MARKING)){
				try{
					m_inicial = Integer.parseInt(nl.item(i).getTextContent().trim());
				} catch (NumberFormatException ex){
					return null;
				}
			}
			else if(currentNodeName.equals(NAME)){
				placeIndex = getPetriObjectIndexFromName(nl.item(i).getTextContent().trim());
			}
		}
		
		if(placeIndex == null){
			return null;
		}
		
		return new Plaza(id, m_inicial, placeIndex);
	}
	
	/**
	 * Parses a node and returns the containing transition as an object
	 * @param id the object id embedded in the PNML
	 * @param transitionNode Node object from PNML
	 * @param nl transitionNode children nodes as NodeList
	 * @return
	 */
	private Transicion getTransition(String id, Node transitionNode, NodeList nl){
		
		final String labelRegexString = "[A-Z]";
		final Pattern labelRegex = Pattern.compile(labelRegexString, Pattern.CASE_INSENSITIVE);
		
		Integer transitionIndex = null;
		//Una transicion es NO automatica y NO informa, a menos que se diga lo contrario
		boolean isAutomatic = false, isInformed = false;
		for(int i=0; i<nl.getLength(); i++){
			String currentNodeName = nl.item(i).getNodeName();
			if(currentNodeName.equals(LABEL)){
				String transitionLabels = nl.item(i).getTextContent().trim();
				Matcher labelMatcher = labelRegex.matcher(transitionLabels);
				while(labelMatcher.find()){
					String label = labelMatcher.group();
					isAutomatic = label.equalsIgnoreCase("A") ? true : isAutomatic;
					isInformed = label.equalsIgnoreCase("I") ? true : isInformed;
				}
			}
			else if(currentNodeName.equals(NAME)){
				transitionIndex = getPetriObjectIndexFromName(nl.item(i).getTextContent().trim());
			}
		}
		
		if(transitionIndex == null){
			// if transitionIndex is null, the PNML is ill-formed
			return null;
		}
		
		return new Transicion(id, new Etiqueta(isAutomatic, isInformed), transitionIndex);
	}
	
	/**
	 * Parses a node and returns the containing arc as an object
	 * @param id the object id embedded in the PNML
	 * @param arcNode Node object from PNML
	 * @param nl arcNode children nodes as NodeList
	 * @return
	 */
	private Arco getArc(String id, Node arcNode, NodeList nl){
		Element arcElement = (Element)arcNode;
		String source = arcElement.getAttribute(SOURCE);
		String target = arcElement.getAttribute(TARGET);
		if(source.isEmpty() || target.isEmpty()){
			return null;
		}
		
		Integer weight = 1;
		for(int i=0; i<nl.getLength(); i++){
			if(nl.item(i).getNodeName().equals(WEIGHT)){
				weight = Integer.parseInt(nl.item(i).getTextContent().trim());
				break;
			}
		}
		
		if(weight < 1){
			return null;
		}
		
		return new Arco(id, source, target, weight);
	}
	
	/**
	 * Parses the place or transition's name (e.g: t10, p5) and returns the index embedded
	 * @param name place or transition name to be parsed
	 * @return place or transition Index
	 */
	private Integer getPetriObjectIndexFromName(String objectName){
		// gets object's name which contains its number
		try{
			// trims the letter away and parses the number
			return Integer.parseInt(objectName.substring(1, objectName.length()));
		} catch (NumberFormatException ex){
			return null;
		}
	}
}