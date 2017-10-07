# Java Petri Concurrency Monitor

This project implements a concurrency monitor written in Java 8.  
JPCM is orchestrated by a user-defiend Petri Net and it's intended to be imported as a library for Java projects.

## Petri Nets:
  A Petri net is a graphical and mathematical model which is a generalization of state machines.  
  Petri nets offer a graphical notation for stepwise processes that include choice, iteration, and concurrent execution. Unlike other standard models, Petri nets have an exact mathematical definition of their execution semantics, with a well-developed mathematical theory for process analysis.  
For further information:
- [Petri Net in Wikipedia] (https://en.wikipedia.org/wiki/Petri_net)
- Petri Nets, Fundamental Models, Verification and Applications - Michel Diaz - 2009, ISBN:18482107959781848210790 

## Monitor
In concurrent programming, a monitor is a thread-safe class, object, or module that uses wrapped mutual exclusion in order to safely allow access to a method or variable by more than one thread. The defining characteristic of a monitor is that its methods are executed with mutual exclusion: At each point in time, at most one thread may be executing any of its methods.  
For further information:
- [Monitor in Wikipedia] (https://en.wikipedia.org/wiki/Monitor_(synchronization))

## Key Features:
- Import from PNML format files ([TINA] (http://projects.laas.fr/tina/) dialect)
- Support for several arc types:
  - Inhibitor arcs
  - Normal arcs
  - Reader/Test arcs
  - Reset arcs
- Support for petri net types:
  - Place/Transition net
  - Timed net
- Support for guards: boolean variable associated to transition enabling condition
- Support for automatic/fired transitions:
  - Fired transitions are explicitly fired by a method call
  - Automatic transitions are fired whenever it's possible
- Support for event listening for informed transitions (through RxJava: https://github.com/ReactiveX/RxJava)
  - Events are sent with transition firing information in JSON format
- Custom thread priority management policies

## Usage:

### File Format
File format is the PNML dialect used by TINA.  
Since TINA doesn't support reset arcs, the user has to change the arc _type_ value to _reset_ manually as in the following example:
```xml
<arc id="e-1" source="p-1" target="t-1">
<type value="reset"/>
</arc>
```

### Labels and Label Syntax
Labels specify atributes for a transition.  
A label is added as an atribute for a transition, under the field _"label"_ and specifies three properties for a transition:  
- Automatic: An automatic transition is executed whenever the sensibilization logic allows it. It doesn't depend on an event.
- Informed: An informed transition sends can be linked to an observer. When it's executed, it sends a JSON formatted event to all its observers.
- Guard: The name and enabling value for a guard related to a transition.

The syntax is the following:
```xml
<automatic_value,informed_value,(guard_name)>
```

where:
- automatic_value (case insensitive) can be:
  - A for automatic transition
  - F or D for non-automatic (fired) transition
- informed_value (case insensitive) can be:
  - I for informed transition
  - N for non-informed transition
- guard_value is the name of the guard associated to this transition:
  - Guards may be shared by any amount of transitions
  - Guard are associated by `false` enabling level if their name is preceeded by ! or ~ token
  - Guard names must comply with Java variable names restrictions

For instance, the label `<F,I,(~foobar)>` specifies the related transition is _fired_, _informed_ and has a guard _foobar_ associated by `false` value.  

All values are optional for labels. To specify any value, all values to its left must be explicit.  
The default values are:
- automatic_value: __A__
- informed_value: __N__
- guard: __None__

### Event Message
When an informed transition is fired, if there is at least one observer suscript to its events, an event message is sent.  
Events are sent in JSON format containing the following info of the fired transition:
- The transition name: given by the user or assigned automatically by TINA editor
- The transition id: assigned automatically by TINA editor
- The transition index: Matching index for the incidence matrix column for this transition. This index is computed internally and it's useful for debugging the Petri Net

#### Event Message Format
 The format is the following:
```json
{
  "name": "transition_name",
  "id": "transition_id",
  "index": "transition_index"
}
```

#### Subscribing and Unsubscribing to a Transition's Events
To subscribe to a transition's event, there must be a concrete implementation of [rx.Observer\<String>](http://reactivex.io/RxJava/javadoc/rx/Observer.html) which overloads method [onNext()](http://reactivex.io/RxJava/javadoc/rx/Observer.html#onNext(T)) to handle event processing.
The suscription may be done using the `Transition` object or its name.  
Let's consider there is a class `ConcreteObserver` that complies with all restrictions. In the following example, two suscriptions are made, one by each method:  
```java
// first transition to suscribe to
Transition t0 = petri.getTransitions()[0];
// name of the second transition to suscribe to
String t1Name = "start_process_01";

Observer<String> observer = new ConcreteObserver();

// suscription made using Transition object
Subscription subscription0 = monitor.subscribeToTransition(t0, observer);

// suscription made using transition name
Subscription subscription1 = monitor.subscribeToTransition(t1Name, observer);
```

Now, observer is subscript to t0's and t1's events.  

The suscription returns a `Suscription` object which is used to cancel the suscription calling `subscription.unsubscribe()`. More info in [Subscription](http://reactivex.io/RxJava/javadoc/rx/Subscription.html).  

### Guards
Guards are boolean variables associated to one or more transitions.  
When a guard is associated to a transition an enabling value is associated too, if the guard's value is different from the expected it will disable its associated transition. For instance, if the guard _fooGuard_ is associated to transition _t0_ with enabling value `true`, setting _fooGuard_ to `false` disables _t0_.  
If a single guard is associated to different transitions it can be used to decide a path to take. In the following figure, the guard _fooGuard_ can decide whether to fire _t0_ or _t1_.  
![](/doc/img/guard_as_decision.png)

> __Note:__ all guards begin with value __false__ at the begining of the program

#### Setting a Guard's Value
The following example code shows how to set the value for a guard _fooGuard_:

```java
  // some code
  
  // there has already been declared a PetriMonitor called monitor
  
  monitor.setGuard("fooGuard", true);
  
  // some more code
  
  monitor.setGuard("fooGuard", false);
  
  // some more code
```

### Setting Up my Petri Net and Monitor
Before using JPCM, the user has to initialize some objects with the Petri Net information.  
Let's assume the Petri Net to use is described in a PNML file in path `"path/to/my/petri/net.pnml"`
This example sets up the environment:

```java
public void setUp() {
  PetriNetFactory factory = new PetriNetFactory("path/to/my/petri/net.pnml");
  PetriNet petri = factory.makePetriNet(petriNetType.PLACE_TRANTISION);
  TransitionsPolicy policy = new FirstInLinePolicy();
  PetriMonitor monitor = new PetriMonitor(petri, policy);
  
  // generate my worker threads here
  
  // if I need an observer, construct it and suscribe it here
  
  // must initialize the petri net before using it
  petri.initializePetriNet();
  
  // launch my worker threads here
  
  // the main thread can do another task
  // while the worker threads are executing
  // It just cannot finish before the,¿m
  
  // for example, let's make the main thread
  // print the petri net current marking every 5 seconds
  
  while(true){
    try{
      Thread.sleep(5000);
      System.out.println(petri.getCurrentMarking());
    } catch (InterruptedException e){
    }
  }
}
```

### Firing a Transition
Firing a transition is the equivalent of asking the monitor for mutual exclusion over a certain resource.  
A thread should fire a transition before trying to access a shared resource and, if needed, after realising it. Also, firing a transition can be used to signal another thread that some task has started or finished.  
A transition fire is performed in mutual exclusion inside the monitor. It's executed by a call to `fireTransition` on an instance of `PetriMonitor`.  
There are two ways of firing a transition, either using the `Transition` object or its name. In the following example two transitions are fired, one by each method:

```java
Thread worker = new Thread( new Runnable() {
  @Override
  public void run() {
    try {
      // instructions to execute outise mutual exclusion
      // fire transition by name
      monitor.fireTransition("TransitionName");
      // some other tasks
      // fire by Transition object
      Transition t0 = petri.getTransition()[0];
      monitor.fireTransition(t0);
      // some other tasks
    } catch (IllegalArgumentException | NotInitializedPetriNetException e) {
      // handle the exceptions
    }
  }
});
```

#### Perennial Fire
There is a second parameter in `PetriMonitor.fireTransition`: `boolean notPerennialFire`. It's default value is `false` and it indicates whether a transition fire is non-perennial.  
If a thread tries to fire a disabled transition perennially, it locks in the associated condition queue (default behaviour). On the other hand, if the fire is non-perennial, the thread just leaves the monitor without firing the transition.  
Non-perennial fire is useful for modeling non-blocking actions. An example of non-blocking action is turning a light on:
- If the light is off, turn it on
- If the light is already on, no action is needed
In any case, the light is on in the end. In the same way , a thread calling a non-perennial fire on a disabled transition won't get blocked.
A thread performing a non-perennial fire on a timed transition can get blocked only if it attempted to do the fire before the enabling interval. This type of block is temporal and the thread will unlock itself when the firing interval starts.

#### Considerations When Firing a Transition
There are a few consideration to take into account when firing a transition:
- If firing a transition was unsuccessfull, the calling thread will sleep until it can fire that transition. So the main thread should never be used to do this.
- Firing an automatic launches an error `IllegalTransitionFiringError`.


### Transitions Policy
When an enabling condition changes (current net marking, or a guard's value), some transitions may get enabled. In this case, automatic transitions have to be fired and threads waiting for fresh enabled fired transitions have to be signaled to resume.  
If an enabling condition change enables just one transition which transition to fire or thread to signal is a trivial question. Otherwise, the _Transitions Policy_ must decide which transition has the highest priority over a list of enabled transitions.  
JPCM comes with two available policies:
 - _FistInLinePolicy_: Chooses the first enabled transition from a given set
 - _RandomPolicy_: Chooses a random enabled transition from a given set

A transitions policy is set to the petri monitor in one of two ways:
- During `PetriMonitor` construction, using a `TransitionsPolicy` implementation in the constructor.
- At runtime, through the method call `PetriMonitor.setTransitionsPolicy(TransitionsPolicy _transitionsPolicy)`

#### Creating My Own Transitions Policy
Any isntance of a class that extends _TransitionsPolicy_ abstrac class is a valid transitions policy. By overriding method `public int which(boolean[] enabled)` the policy can tell the monitor which transition should be fired next from a given set where:
- `enabled` array is ordered by transition index (the same order given by `PetriNet.getTransitions()`). That way, if `enabled[i]` is `true`, the i<sup>th</sup> transition is enabled.
- The retun value is the index of the next transition to be fired or -1 if there are no enabled transitions.

In the following example a custom policy is defined at initialization time:
```java
  // some code
  
  // let's assume there is a Petri object called petri
  
  PetriMonitor monitor = new PetriMonitor(petri, new TransitionsPolicy(petri) {
  
    @Override
    public int which(boolean[] enabled){
      int ret = 0;
      // fill ret with some criteria
      return ret;
    }
    
  };
  
  // some more code
```

In the next example a class implements a transitions policy with a static order defined by transitions names:

```java
public class OrderedPrioritiesPolicy extends TransitionsPolicy {
  
  private int[] priorityArray = {
    petri.getTransition("fin_proceso_01").getIndex(),
    petri.getTransition("fin_proceso_02").getIndex(),
    petri.getTransition("comienzo_proceso_01").getIndex(),
    petri.getTransition("comienzo_proceso_02").getIndex()
  };

  public OrderedPrioritiesPolicy(PetriNet _petri){
    super(_petri)
  }

  @Override
  public int which(boolean[] enabled) {
    for(int index : priorityArray) {
    if(enabled[index]) {
      return index;
    }
  }
  return -1;
  }
  
}
```

> __Note:__ An incorrect implementation of a custom policy may cause to thread starving

> __Note:__ If there is at least one `true` value in the `enabled` vector, method `which` must not return -1. This way one or more threads would starve 
