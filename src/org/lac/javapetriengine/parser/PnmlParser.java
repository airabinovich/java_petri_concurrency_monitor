package org.lac.javapetriengine.parser;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.lac.javapetriengine.errors.DuplicatedIDError;
import org.lac.javapetriengine.errors.DuplicatedNameError;
import org.lac.javapetriengine.exceptions.BadPnmlFormatException;
import org.lac.javapetriengine.petrinets.components.Arc;
import org.lac.javapetriengine.petrinets.components.Label;
import org.lac.javapetriengine.petrinets.components.PetriNode;
import org.lac.javapetriengine.petrinets.components.Place;
import org.lac.javapetriengine.petrinets.components.TimeSpan;
import org.lac.javapetriengine.petrinets.components.Transition;
import org.lac.javapetriengine.petrinets.components.Arc.ArcType;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.lang.Integer;

public class PnmlParser{
	
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
	private static final String ARC_TYPE = "type";
	private static final String VALUE = "value";
	
	private File pnmlFile;
	
	private int placesIndex;
	private int transitionsIndex;
	
	public PnmlParser(String pnmlPath) throws FileNotFoundException, SecurityException, NullPointerException{
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
	 * @throws BadPnmlFormatException
	 * @throws DuplicatedNameError If two or more places or transitions share names (empty names are not a problem)
	 * @throws DuplicatedIDError If two or more places or transitions share ids
	 */
	public Triplet<Place[], Transition[], Arc[]> parseFileAndGetPetriComponents() throws BadPnmlFormatException, DuplicatedNameError, DuplicatedIDError {
		try {
			placesIndex = 0;
			transitionsIndex = 0;
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
					ret = getPetriComponentsFromNodeList(netElements);
				}
			}
			
			return ret;
	    } catch (SAXException | IOException | ParserConfigurationException e) {
	    	return null;
	    }
	}
	
	/**
	 * Parses a list of nodes and returns information about places, transitions and arcs embedded
	 * @param netElements a list of nodes children of page node
	 * @return a Triplet containing all places, transitions and arcs inside netElements
	 * @throws BadPnmlFormatException 
	 * @throws DuplicatedNameError If two or more places or transitions share names (empty names are not a problem)
	 * @throws DuplicatedIDError If two or more places or transitions share ids
	 */
	private Triplet<Place[], Transition[], Arc[]> getPetriComponentsFromNodeList(NodeList netElements) throws BadPnmlFormatException, DuplicatedNameError, DuplicatedIDError{
		ArrayList<Place> places = new ArrayList<Place>();
		ArrayList<Transition> transitions = new ArrayList<Transition>();
		ArrayList<Arc> arcs = new ArrayList<Arc>();
		ArrayList<Triplet<String, Node, NodeList>> pendingArcs = new ArrayList<Triplet<String, Node, NodeList>>();
		for(int index = 0; index < netElements.getLength(); index++){
			Node child = netElements.item(index);
			if(child.getNodeType() == Node.ELEMENT_NODE ){
				NodeList nl = child.getChildNodes();
				String id = ((Element)(child)).getAttribute(ID);
				//child has the element to parse (place, transition or arc)
				if(child.getNodeName().equals(PLACE)){
					places.add(getPlace(id, child, nl, places));
				}
				else if(child.getNodeName().equals(TRANSITION)){
					transitions.add(getTransition(id, child, nl, transitions));
				}
				else if(child.getNodeName().equals(ARC)){
					// leave arcs for the last. We'll need to know if the source and target id matches a place or a transition
					pendingArcs.add(new Triplet<String, Node, NodeList>(id, child,nl));
				}
			}
		}
		
		// Just in case, let's order the places and transitions by index. It'll be needed for matrices generation
		places.sort((Place p1, Place p2) -> p1.getIndex() - p2.getIndex());
		transitions.sort((Transition t1, Transition t2) -> t1.getIndex() - t2.getIndex());
		
		for(Triplet<String, Node, NodeList> arcData : pendingArcs){
			arcs.add(getArc(arcData.getValue0(), arcData.getValue1(), arcData.getValue2(), places, transitions));
		}
		
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
	 * @param places An arrayList containing all places known until
	 * @return A place object containing the info parsed
	 * @throws BadPnmlFormatException If initial marking is not numerical
	 * @throws DuplicatedNameError If the new place has the same name as any created before
	 * @throws DuplicatedIDError If the new place has the same id as any created before
	 * @see {@link org.lac.javapetriengine.petrinets.components.Place}
	 */
	private Place getPlace(String id, Node placeNode, NodeList nl, final ArrayList<Place> places) throws BadPnmlFormatException, DuplicatedNameError, DuplicatedIDError{
		Integer m_inicial = 0;
		Integer placeIndex = this.placesIndex++;
		String placeName = "";
		for(int i=0; i<nl.getLength(); i++){
			String currentNodeName = nl.item(i).getNodeName();
			if(currentNodeName.equals(INITIAL_MARKING)){
				try{
					m_inicial = Integer.parseInt(nl.item(i).getTextContent().trim());
				} catch (NumberFormatException ex){
					throw new BadPnmlFormatException("Error parsing place initial marking, not numerical");
				}
			}
			else if(currentNodeName.equals(NAME)){
				placeName = nl.item(i).getTextContent().trim();
			}
		}
		
		for(Place p : places){
			String pName = p.getName();
			if(!pName.isEmpty() && pName.equalsIgnoreCase(placeName)){
				throw new DuplicatedNameError("Name " + placeName + " is repeated for places");
			}
			if(p.getId().equals(id)){
				throw new DuplicatedIDError("ID " + id + " is repeated for places");
			}
		}
		
		if(placeName.isEmpty()){
			placeName = "p" + placesIndex;
		}
		
		return new Place(id, m_inicial, placeIndex, placeName);
	}
	
	/**
	 * Parses a node and returns the containing transition as an object
	 * @param id the object id embedded in the PNML
	 * @param transitionNode Node object from PNML
	 * @param nl transitionNode children nodes as NodeList
	 * @param transitions An arraylist containing all transitions known
	 * @return A transition object containing the info parsed
	 * @throws BadPnmlFormatException
	 * @throws DuplicatedNameError If the new transition has the same name as any created before
	 * @throws DuplicatedIDError If the new transition has the same id as any created before 
	 * @see {@link org.lac.javapetriengine.petrinets.components.Transition}
	 */
	private Transition getTransition(String id, Node transitionNode, NodeList nl, ArrayList<Transition> transitions) throws BadPnmlFormatException, DuplicatedNameError, DuplicatedIDError{
		
		TimeSpan timeSpan = null;
		Label label = null;
		Pair<String, Boolean> guard = null;
		Integer transitionIndex = this.transitionsIndex++;
		String transitionName = "";
		
		for(int i=0; i<nl.getLength(); i++){
			String currentNodeName = nl.item(i).getNodeName();
			if(currentNodeName.equals(LABEL)){
				try{
					Pair<Label,Pair<String, Boolean>> labelAndGuard = 
							getLabelAndGuardFromNode(nl.item(i).getTextContent().trim());
					label = labelAndGuard.getValue0();
					guard = labelAndGuard.getValue1();
				} catch (DOMException e){
					throw new BadPnmlFormatException("Error parsing label in PNML file");
				}
			}
			else if(currentNodeName.equals(NAME)){
				transitionName = nl.item(i).getTextContent().trim();
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
		
		if(label == null) {
			// if no label was found, by default it's fired and not informed
			label = new Label(false, false);
		}
		
		if(guard == null){
			// if no guard was found, let's create an empty one
			guard = new Pair<String, Boolean>(null, false);
		}
		
		for(Transition t : transitions){
			String tName = t.getName();
			if(!tName.isEmpty() && tName.equalsIgnoreCase(transitionName)){
				throw new DuplicatedNameError("Name " + transitionName + " is repeated for transitions");
			}
			if(t.getId().equals(id)){
				throw new DuplicatedIDError("Id " + id + " is repeated for transitions");
			}
		}
		
		if(transitionName.isEmpty()){
			// if no name specified, by default is t#
			transitionName = "t" + transitionsIndex;
		}
		
		return new Transition(id, label, transitionIndex, timeSpan, guard, transitionName);
	}
	
	/**
	 * Parses a node and returns the containing arc as an object
	 * @param id the object id embedded in the PNML
	 * @param arcNode Node object from PNML
	 * @param nl arcNode children nodes as NodeList
	 * @param places An ArrayList containing the places parsed from the PNML
	 * @param transitions An ArrayList containing the transitions parsed from the PNML
	 * @return Arc object containing the info parsed
	 * @throws BadPnmlFormatException if any of these errors occur:
	 * <li> Source id or Target id are empty </li>
	 * <li> Weight is a non-numeric string </li>
	 * <li> Weight is less than 1 </li>
	 * <li> A source or target id doesn't match any place nor transition existing </li>
	 * <li> Source and Transition are both places </li>
	 * <li> Source and Transition are both transitions </li>
	 * <li> A non-normal arc goes from transition to place </li>
	 * @see {@link org.lac.javapetriengine.petrinets.components.Arc}
	 */
	private Arc getArc(String id, Node arcNode, NodeList nl, ArrayList<Place> places, ArrayList<Transition> transitions) throws BadPnmlFormatException{
		Element arcElement = (Element)arcNode;
		String sourceId = arcElement.getAttribute(SOURCE);
		String targetId = arcElement.getAttribute(TARGET);
		PetriNode source;
		PetriNode target;
		if(sourceId.isEmpty() || targetId.isEmpty()){
			throw new BadPnmlFormatException("Error parsing arcs, invalid source or target found");
		}

		Integer weight = 1;
		ArcType type = ArcType.NORMAL;

		try{
			for(int i=0; i<nl.getLength(); i++){
				Node item = nl.item(i);
				String itemName = item.getNodeName();
				if(itemName.equals(WEIGHT)){
					weight = Integer.parseInt(nl.item(i).getTextContent().trim());
				} else if (itemName.equals(ARC_TYPE)){
					type = ArcType.fromString(((Element)item).getAttribute(VALUE));
				}
			}
		} catch (NumberFormatException e){
			throw new BadPnmlFormatException("Error parsing arcs", e);
		}

		if(weight < 1){
			throw new BadPnmlFormatException("Negative or zero weight found parsing arc");
		}
		
		boolean sourceIsPlace = false;
		boolean sourceIsTransition = false;
		boolean targetIsPlace = false;
		boolean targetIsTransition = false;
		
		source = getFirstFromFilteredList(places, (Place p) -> p.getId().equals(sourceId));
		sourceIsPlace = source != null;
		
		if(!sourceIsPlace){
			
			source = getFirstFromFilteredList(transitions, (Transition t) -> t.getId().equals(sourceId));
			sourceIsTransition = source != null;
			
			if(!sourceIsTransition){
				throw new BadPnmlFormatException("Arc source id doesn't match any place nor transition for arc " + id);
			}
		}
		
		target = getFirstFromFilteredList(places, (Place p) -> p.getId().equals(targetId));
		targetIsPlace = target != null;
		
		if(!targetIsPlace){
			
			target = getFirstFromFilteredList(transitions, (Transition t) -> t.getId().equals(targetId));
			targetIsTransition = target != null;
			
			if(!targetIsTransition){
				throw new BadPnmlFormatException("Arc transition id doesn't match any place nor transition for arc " + id);
			}
		}
		
		if(sourceIsPlace && targetIsPlace){
			throw new BadPnmlFormatException("Arc " + id + " goes from place to place");
		}
		
		if(sourceIsTransition && targetIsTransition){
			throw new BadPnmlFormatException("Arc " + id + " goes from transition to transition");
		}
		
		if(type != ArcType.NORMAL && targetIsPlace){
			throw new BadPnmlFormatException("Arc " + id + " of type " + type + " is input to a place");
		}

		return new Arc(id, source, target, weight, type);
	}
	
	/**
	 * Filters a list using the given predicate and returns the first element that matches the condition if any.
	 * This is typically used for filtering places or transitions
	 * @param list The list of PetriNode objects to filter
	 * @param predicate The filtering function
	 * @return A PetriNode object matching the predicate condition or null if none matches
	 */
	private <E extends PetriNode> E getFirstFromFilteredList(ArrayList<E> list, Predicate<E> predicate){
		
		Stream<E> streamList = list.stream();
		
		Optional<E> filteredElement = streamList.filter(predicate).findFirst();
		
		return filteredElement.orElse(null);
		
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
				if(timeE < Long.MAX_VALUE){
					timeE -= (closure.equals(OPEN) || closure.equals(CLOSEDOPEN)) ? 1 : 0;
				}
			}
		}
		return new TimeSpan(timeB, timeE);
	}
	
	/**
	 * Get transition label from text label. Labels order:
	 * <li> Automatic(A) or Fired(F,D) </li>
	 * <li> Informed(I) or Not Informed(N) </li>
	 * <li> Guards (variable name between brackets, ~ and ! are NOT operator, whitespaces will be trimmed if any) </li>
	 * @param labelInfo The label in string format
	 * @return A Label object containing the info included in labelInfo
	 * @throws BadPnmlFormatException if the string doesn't respect the label format
	 */
	private Pair<Label,Pair<String, Boolean>> getLabelAndGuardFromNode(String labelInfo) throws BadPnmlFormatException{
		
		if(labelInfo.charAt(0) != '<' || labelInfo.charAt(labelInfo.length() - 1) != '>'){
			// label must be enclosed by "<" and ">"
			throw new BadPnmlFormatException("Error parsing labels in PNML file");
		}
		final int AUTOMATIC_INDEX = 0;
		final int INFORMED_INDEX = 1;
		final int GUARD_INDEX = 2;
		
		// by default no guard
		Pair<String, Boolean> guard = new Pair<String, Boolean>(null, null);
		
		String labelStr = labelInfo.substring(1, labelInfo.length() - 1);
		boolean isAutomatic = false;
		boolean isInformed = false;
		
		String[] labels = labelStr.split(",");
		for( int i = 0; i < labels.length; i++ ){
			String label = labels[i];
			switch(i){
			case AUTOMATIC_INDEX:
				if( !label.equalsIgnoreCase("A") && !label.equalsIgnoreCase("D") && !label.equalsIgnoreCase("F")){
					throw new BadPnmlFormatException("Wrong automatic label: " + label);
				}
				isAutomatic = label.equalsIgnoreCase("A");
				break;
			case INFORMED_INDEX:
				if( !label.equalsIgnoreCase("I") && !label.equalsIgnoreCase("N")){
					throw new BadPnmlFormatException("Wrong informed label: " + label);
				}
				isInformed = label.equalsIgnoreCase("I");
				break;
			case GUARD_INDEX:
				try{
					if(label.charAt(0) != '(' || label.charAt(label.length() - 1) != ')'){
					// guard must be enclosed by brackets
					throw new BadPnmlFormatException("Bad formatted guard in " + label
							+ "from label " + labels);
					}
					// trim the brackets
					String guardStr = label.substring(1, label.length() - 1).replaceAll("\\s", "");
					//check if it's for negative logic
					boolean negative = (guardStr.charAt(0) == '~' || guardStr.charAt(0) == '!');
					if (negative){
						//discard first char "~"
						guardStr = guardStr.substring(1);
						guard = guard.setAt1(false);
					} else {
						guard = guard.setAt1(true);
					}
					guard = guard.setAt0(guardStr);
				} catch (IndexOutOfBoundsException e){
					// nothing wrong, just empty guard
				}
			default:
				break;
			}
		}
		
		return new Pair<Label,Pair<String, Boolean>>(new Label(isAutomatic, isInformed), guard);
	}
}
