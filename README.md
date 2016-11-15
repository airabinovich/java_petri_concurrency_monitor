# Java Petri Concurrency Monitor

This is an implementation of a concurrency monitor written in Java 8.  
The key feature of this concurrency monitor, is that it's orchestrated by a user-defiend Petri Net.  
This implementation is intended to be imported as a library in Java projects.

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
  - Fired transitions are explicitly fired by a thread
  - Automatic transitions are fired whenever it's possible
- Support for event listening for informed transitions (through RxJava: https://github.com/ReactiveX/RxJava)
  - Events are sent with transition firing information in JSON format
- Extendible thread priority management policy

## Usage:

### File Format
File format is the PNML dialect used by TINA.  
Since TINA doesn't support inhibitor arcs, the user has to change the arc _type_ value to _inhibitor_ manually

#### Labels and Label Syntax
Labels specify atributes for a transition.  
A label is added as an atribute for a transition, under the field "label".  
The syntax is the following:
> \<automatic_value,informed_value,(guard_name)\>  

where:
- automatic_value (case insensitive) can be:
  - A for automatic transition
  - F or D for fired transition
- informed_value (case insensitive) can be:
  - I for informed transition
  - N for non-informed transition
- guard_value is the name of the guard associated to this transition:
  - Guards can be shared by any amount of transitions and can be negated using ! or ~ token before the guard name
  - Guard names must comply to variable names restrictions:
    - Must start with letter or underscore
    - May contain numbers, letters, underscore or dollar sign
    - Are case sensitive

All values are optional for labels but to specify any value, all values to its left must be specified.  
The default values are:
- automatic_value: A
- informed_value: N
- guard: None

For example:
> \<F,I,(~foobar)\>

This label specifies that the associated transition is __Fired__, __Informed__, and has a guard called __foobar__ which enables the transition by __false__ value

### Setting Up my Petri Net and Monitor
Before using this Java Petri Concurrency Monitor, the user has to set up some classes.
Given I have my petri net ready to use, and it's in a PNML file in path "path/to/my/petri/net.pnml"
This example will set up my environment:

```java
public void setUp() {
  PetriNetFactory factory = new PetriNetFactory("path/to/my/petri/net.pnml");
  PetriNet petri = (TimedPetriNet) factory.makePetriNet(petriNetType.PT);
  TransitionsPolicy policy = new FirstInLinePolicy();
  PetriMonitor monitor = new PetriMonitor(petri, policy);
  
  // generate my worker threads here
  
  petri.initializePetriNet(); // never forget to initialize the petri net before using it
  
  // launch my worker threads here
  
  // do something in the main thread or lock waiting for worker threads to finish
  // for example, print the petri net current marking every 5 seconds
  
  while(true){
    try{
      Thread.sleep(5000);
      System.out.println(petri.getCurrentMarking());
    } catch (InterruptedException e){
    }
  }
}
```

### Event Message
When an informed transition is fired, if there is any observer suscript to its events, an event message is sent.  
Events are sent in JSON format specifying info of the fired transition. The information in the message is:
- The transition name: a friendly name given by the user or assigned automatically by TINA editor
- The transition id: an id assigned automatically by TINA editor
- The transition idex: an index computed internally. It matches the petri matrices column for this transition. It's useful for debugging

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
To subscribe to a transition's event, there must be a concrete implementation of [rx.Observer\<String>] (http://reactivex.io/RxJava/javadoc/rx/Observer.html) which overloads handlers for [onNext()] (http://reactivex.io/RxJava/javadoc/rx/Observer.html#onNext(T)), [onCompleted()] (http://reactivex.io/RxJava/javadoc/rx/Observer.html#onCompleted()) and [onError()] (http://reactivex.io/RxJava/javadoc/rx/Observer.html#onError(java.lang.Throwable)) methods.
As an example, let's consider there is a class ConcreteObserver that complies with all of the above.

Subscribing by Transition object:
```java
  // some code
  
  Transition t0 = petri.getTransitions()[0]; // transition to suscript to
  Observer<String> observer = new ConcreteObserver();
  Subscription subscription = monitor.subscribeToTransition(t0, observer);
  
  // some more code
```

Subscribing by Transition name:
```java
  // some code
  // let's assume t0's name is "start_process_01"
  Observer<String> observer = new ConcreteObserver();
  Subscription subscription = monitor.subscribeToTransition("start_process_01", observer);
  
  // some more code
```

Now, observer is subscript to t0's events.  

To unsubscribe, simply call `subscription.unsubscribe()` from [Subscription] (http://reactivex.io/RxJava/javadoc/rx/Subscription.html) interface,

### Firing a Transition
Firing a transition is the equivalent of asking the monitor for mutual (or shared) exclusion over a certain resource.  
A thread should fire a transition before trying to access a shared resource and, if needed, after realising it. Also, firing a transition can be used to signal another thread that some task has started or finished.
Firing a transition is done inside PetriMonitor class by calling one of the fireTransition method overloads.

There are two ways to fire a transition:
- By name:  
  Java 7 style:
```java
Thread worker = new Thread( new Runnable() {
  @Override
  public void run() {
    try {
      // non-exlusive tasks
      monitor.fireTransition("SomeTransitionName");
      // do some other task
      // maybe fire another transition if needed
    } catch (IllegalArgumentException | NotInitializedPetriNetException e) {
      // handle the exceptions
    }
  }
});
worker.start();
```
  Java 8 style:
```java
Thread worker = new Thread(() -> {
  try {
    monitor.fireTransition("SomeTransitionName");
    // do some other task
  } catch (IllegalArgumentException | NotInitializedPetriNetException e) {
    // handle the exceptions
  }
});
worker.start();
```
- Using the Transition object:  
  Java 7 style:
```java
Thread worker = new Thread( new Runnable() {
  @Override
  public void run() {
    try {
      Transition t0 = petri.getTransition()[0];
      monitor.fireTransition(t0);
      // do some other task
    } catch (IllegalArgumentException | NotInitializedPetriNetException e) {
      // handle the exceptions
    }
  }
});
worker.start();
```
  Java 8 style:
```java
Thread worker = new Thread(() -> {
  try {
    Transition t0 = petri.getTransition()[0];
    monitor.fireTransition(t0);
    // do some other task
  } catch (IllegalArgumentException | NotInitializedPetriNetException e) {
    // handle the exceptions
  }
});
worker.start();
```

#### Perennial Fire
There is an aditional parameter to fireTransition that was ommited in the examples, this is `boolean perennialFire`  
When this parameter is set to true, the fire call is perennial.  
This means, when a thread calls `monitor.fireTransition(t0, true)`:
- If t0 is enabled, the perenial fire behaves exaclty as a regular one
- If t0 is disabled
  - The transition won't be fired (as usual)
  - The thread won't wait for t0 to get enabled and will exit the monitor immediatly
  - The thread will only wait for timed transitions when the fire attempt time is before t0's timespan

#### Considerations When Firing a Transition
There are a few consideration to take into account when firing a transition:
- If firing a transition was now successfull, the calling thread will sleep until it can fire that transition. It's not recommended using the main thread to do this because the program execution may fall into a deadlock situation.
- Firing an automatic transition is a severe error. If doing so, IllegalTransitionFiringError will be thrown

### Guards
Guards are boolean variables associated to one or more transitions.  
When a guard is associated to a transition an enabling value is associated too, if the guard's value is different from the expected it will disable its associated transition. For instance, if the guard _fooGuard_ is associated to transition _to_ with enabling value __true__, setting _fooGuard_ to __false__ disables _t0_.  
If a single guard is associated to different transitions it can be used to decide a path to take. In the following figure, the guard _fooGuard_ can decide whether to fire _t0_ or _t1_.  
![](/doc/img/guard_as_decision.png)

> __Note:__ all guards begin with value __false__ at the begining of the program

#### Setting a Guard's Value
As an example, let's assume guard _fooGuard_ is declared in the PNML file. The following example code shows how to set its value:

```java
  // some code
  
  // there has already been declared a PetriMonitor called monitor
  
  monitor.setGuard("fooGuard", true);
  
  // some more code
  
  monitor.setGuard("fooGuard", false);
  
  // some more code
```

### Transitions Policy
When an enabling condition changes (current net marking, or a guard's value), some transitions may get enabled. If this happens, automatic transitions have to be fired and threads waiting for fresh enabled fired transitions have to be signaled to resume.  
If an enabling condition change enables just one transition there is no question on which transition to fire or thread to signal, but what if two or more transitions were enabled? Which one should be fired? Which thread should be signaled? And how about if the fresh enabled transitions are some automatic and some fired? Which ones should have the higher priority?  
That's what _Transitions Policy_ is for. A policy can decide which transition has the highest priority over a list of enabled transitions.
Out of the box, there are two available policies: _RandomPolicy_ and _FistInLinePolicy_
A transitions policy is set to the petri monitor in one of two ways:
- At construction time giving the policy object to PetriMonitor constructor
- At any future moment using method `setTransitionsPolicy(TransitionsPolicy _transitionsPolicy)` from PetriMonitor

#### Creating My Own Policy
Any class that extends _TransitionsPolicy_ interface and implements its methods can be used as a policy.  
By overriding method `public int which(boolean[] enabled)` the policy will tell the monitor which of the candidate transitions should be fired next. The candidate transitions are flagged as __true__ in _enabled_ array passed as argument. The position in the array matches the transition's index.  

The following is an example on how to set a custom policy at initialization time:
```java
  // some code
  
  // let's assume there is a Petri object called petri
  
  PetriMonitor monitor = new PetriMonitor(petri, new TransitionsPolicy(){
  
    @Override
    public int which(boolean[] enabled){
      int ret = 0;
      // fill ret with some criteria
      return ret;
    }
    
  };
  
  // some more code
```

> __Note:__ An incorrect implementation of a custom policy might lead to deadlock or livelock situations
