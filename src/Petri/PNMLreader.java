package Petri;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.javatuples.Triplet;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import Petri.Arc;
import Petri.Label;
import Petri.Place;
import Petri.Transition;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.lang.Integer;

public class PNMLreader{
	
	private static final String ID = "id";
	private static final String PAGE = "page";
	private static final String NAME = "name";
	private static final String NET = "net";
			
	private static final String PLACE = "place";
	private static final String INITIAL_MARKING = "initialMarking";
	
	private static final String TRANSITION = "transition";
	private static final String LABEL = "label";
	private static final String DELAY = "delay";
	private static final String INTERVAL = "interval";
	private static final String CLOSURE = "closure";
	private static final String OPEN = "open";
	private static final String OPENCLOSED = "open-closed";
	private static final String CLOSEDOPEN = "closed-open";
	private static final String INFTY = "infty";
	
	private static final String ARC = "arc";
	private static final String WEIGHT = "inscription";
	private static final String SOURCE = "source";
	private static final String TARGET = "target";
	
	File pnmlFile;
	
	public PNMLreader(String pnmlPath) throws FileNotFoundException, SecurityException, NullPointerException{
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
	public Triplet<Place[], Transition[], Arc[]> parseFileAndGetPetriObjects(){
		try {
			Triplet<Place[], Transition[], Arc[]> ret = null;
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
	    	return null;
	    }
	}
	
	/**
	 * Parses a list of nodes and returns information about places, transitions and arcs embedded
	 * @param netElements a list of nodes children of page node
	 * @return a Triplet containing all places, transitions and arcs inside netElements
	 */
	private Triplet<Place[], Transition[], Arc[]> getPetriObjectsFromNodeList(NodeList netElements){
		ArrayList<Place> places = new ArrayList<Place>();
		ArrayList<Transition> transitions = new ArrayList<Transition>();
		ArrayList<Arc> arcs = new ArrayList<Arc>();
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
		
		places = checkFixAndSortElements(places);
		transitions = checkFixAndSortElements(transitions);
		
		Place[] retPlaces = new Place[places.size()];
		Transition[] retTransitions = new Transition[transitions.size()];
		Arc[] retArcs = new Arc[arcs.size()];

		retPlaces = places.toArray(retPlaces);
		retTransitions = transitions.toArray(retTransitions);
		retArcs = arcs.toArray(retArcs);
		
		return new Triplet<Place[], Transition[], Arc[]>(retPlaces, retTransitions, retArcs);
	}
	
	/**
	 * Parses a node and returns the containing place as an object
	 * @param id the object id embedded in the PNML
	 * @param placeNode Node object from PNML
	 * @param nl placeNode children nodes as NodeList
	 * @return
	 */
	private Place getPlace(String id, Node placeNode, NodeList nl){
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
		
		return new Place(id, m_inicial, placeIndex);
	}
	
	/**
	 * Parses a node and returns the containing transition as an object
	 * @param id the object id embedded in the PNML
	 * @param transitionNode Node object from PNML
	 * @param nl transitionNode children nodes as NodeList
	 * @return
	 */
	private Transition getTransition(String id, Node transitionNode, NodeList nl){
		
		TimeSpan timeSpan = null;
		Label label = null;		
		Integer transitionIndex = null;
		
		for(int i=0; i<nl.getLength(); i++){
			String currentNodeName = nl.item(i).getNodeName();
			if(currentNodeName.equals(LABEL)){
				label = getLabelFromNode(nl.item(i).getTextContent().trim());
			}
			else if(currentNodeName.equals(NAME)){
				transitionIndex = getPetriObjectIndexFromName(nl.item(i).getTextContent().trim());
			}
			else if(currentNodeName.equals(DELAY)){
				NodeList delay = nl.item(i).getChildNodes();
				for(int j=0; j<delay.getLength(); j++){
					Node currentNode = delay.item(j);
					if(currentNode.getNodeName().equals(INTERVAL)){
						//get the time interval
						timeSpan = getTimeSpanFromNode(currentNode.getTextContent().trim().replace(" ",""), currentNode.getAttributes());
						break;
					}
				}				
			}
		}
		
		if(transitionIndex == null){
			// if transitionIndex is null, the PNML is ill-formed
			return null;
		}
		
		if (label == null) {
			// if no label was found, by default is't fired and not informed
			label = new Label(false, false);
		}

		
		return new Transition(id, label, transitionIndex, timeSpan);
	}
	
	/**
	 * Parses a node and returns the containing arc as an object
	 * @param id the object id embedded in the PNML
	 * @param arcNode Node object from PNML
	 * @param nl arcNode children nodes as NodeList
	 * @return
	 */
	private Arc getArc(String id, Node arcNode, NodeList nl){
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
		
		return new Arc(id, source, target, weight);
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
	
	/**
	 * Sorts the elements (places or transitions) in petriNodes by index
	 * If an index is missing,new indexes are specified
	 * @param petriNodes An ArrayList containing the items to analyze
	 * @return The same ArrayList but sorted and with fixed indexes
	 * @see Petri.PetriNode
	 * @see Petri.Place
	 * @see Petri.Transition
	 */
	private <E extends PetriNode> ArrayList<E> checkFixAndSortElements(ArrayList<E> petriNodes){
		if(petriNodes == null){
			System.out.println("NULL petriNodes");
			return null;
		}
		
		if(petriNodes.isEmpty()){
			System.out.println("EMPTY petriNodes");
			return petriNodes;
		}
		
		petriNodes.sort((E node1, E node2) -> (node1.getIndex() - node2.getIndex()));
		
		int patternIndex = 0;
		boolean needToFix = false;
		
		for(E node : petriNodes){
			if(node.getIndex() != patternIndex){
				needToFix = true;
				break;
			}
			patternIndex++;
		}
		
		if(!needToFix){
			return petriNodes;
		}
		
		patternIndex = 0;

		for(E node : petriNodes){
			node.setIndex(patternIndex);
			patternIndex++;
		}
		
		return petriNodes;	
	}
	
	/**
	 * Builds and returns a TimeSpan object with info contained in attributes
	 * @param interval a string containing the time interval (begin,end)
	 * @param attributes the PNML attributes from {@link #INTERVAL} node
	 * @return TimeSpan object for timed Transition time span
	 */
	private TimeSpan getTimeSpanFromNode(String interval, NamedNodeMap attributes){
		
		long timeB = 0;
		long timeE = 0;
		
		int indexOf = interval.indexOf("\n");
		timeB = Long.parseLong(interval.substring(0, indexOf),10);
		if(interval.substring(indexOf+1).equals(INFTY)){
			timeE = Long.MAX_VALUE;
		}
		else{
			timeE = Long.parseLong(interval.substring(indexOf+1));
		}
		//depending the closure, we will add or subtract the minimum long (1)
		//and all closures will be handled as closed
		for(int k=0; k<attributes.getLength(); k++){
			Node node = attributes.item(k);
			if(node.getNodeName().equals(CLOSURE)){
				String closure = node.getTextContent();
				timeB += (closure.equals(OPEN) || closure.equals(OPENCLOSED)) ? 1 : 0;
				timeE -= (closure.equals(OPEN) || closure.equals(CLOSEDOPEN)) ? 1 : 0;
			}
		}
		return new TimeSpan(timeB, timeE);
	}
	
	/**
	 * Get transition label from text label
	 * @param labelInfo The label in string format
	 * @return A Label object containing the info included in labelInfo
	 */
	private Label getLabelFromNode(String labelInfo){
		final Pattern labelRegex = Pattern.compile("[A-Z]", Pattern.CASE_INSENSITIVE);
		boolean isAutomatic = false;
		boolean isInformed = false;
		
		Matcher labelMatcher = labelRegex.matcher(labelInfo);
		while(labelMatcher.find()){
			String label = labelMatcher.group();
			isAutomatic = label.equalsIgnoreCase("A") ? true : isAutomatic;
			isInformed = label.equalsIgnoreCase("I") ? true : isInformed;
		}
		
		return new Label(isAutomatic, isInformed);
	}
}
