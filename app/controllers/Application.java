package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.data.*;
import static play.data.Form.*;
import play.data.validation.Constraints.*;
import play.mvc.Controller;
import views.html.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.TimedConsoleProgressMonitor;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasoner;
import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;


public class Application extends Controller {
    
	
    static ArrayList<String> model = new ArrayList<String>();
    static ArrayList<String> beeModel = new ArrayList<String>();	     
    static ArrayList<String> CharacterList = new ArrayList<String>();
    static ArrayList<String> BeesList = new ArrayList<String>();
    public static ArrayList<String> KeyDescriptionArraylist = new ArrayList<String>();
    
    static int charCount;
    static int beeCountnumber;
	
    public static ArrayList<String> List1Comments = new ArrayList<String>();
    public static ArrayList<String> CharacterArraylist = new ArrayList<String>();
	String Path;
	
    //Ontology variables declaration
    public static OWLOntologyManager manager;
	public static File BeeOntologyfile;
	public static OWLOntology BeeCharacteristics;
	public static OWLDataFactory factory1;
	public static ManchesterOWLSyntaxOWLObjectRendererImpl renderer;
	public static FaCTPlusPlusReasonerFactory fppRF;
	public static TimedConsoleProgressMonitor progressMonitor;
	public static OWLReasonerConfiguration config;
	public static OWLReasoner reasoner;
	
    
    /**
     * The Ontology form.
     */
    public static class Ontology {
        @Required public ArrayList<String> features;
        public ArrayList<String> selectedFeatures;
        public ArrayList<String> results;
        public ArrayList<Boolean> selected;
        
        public Ontology(ArrayList<String> features, 
        		ArrayList<String> selectedFeatures, 
        		ArrayList<String> results ){
        	this.features = features;
        	this.selectedFeatures = selectedFeatures;
        	this.results = results;
        }
    }
    
    // -- Actions
    
    
    public static Result home(){
    	return ok(index.render(""));
    }
  
    /**
     * Handles the file upload.
     * @throws OWLOntologyCreationException 
     * @throws InterruptedException 
     */
    public static Result uploadFile() throws OWLOntologyCreationException, InterruptedException {
    	
    	Form<Ontology> ontologyForm = form(Ontology.class);
    	
    	MultipartFormData body = request().body().asMultipartFormData();
    	  FilePart ontologyFile = body.getFile("ontology");
    	  if (ontologyFile != null) {
    	   String fileName = ontologyFile.getFilename();
    	   String contentType = ontologyFile.getContentType(); 
    	   // ontology.get
    	   File file = ontologyFile.getFile();
    	    
    	   try{ 
    		   loadOntology(file.getPath());
    	   }
    	   catch(UnparsableOntologyException ex){
    		   return ok(index.render("Not a valid Ontology File"));
    	   }
    	  
    	  //Initiate the reasoner to classify ontology
    	  if (BeeOntologyfile.exists())
    	  {
    		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
    		
    		//System.out.println("suppp");
    		loadBeeCharacteristics();	
    	  }
    	    
    	  Ontology ontology = new Ontology(KeyDescriptionArraylist, null, null);
    	  return ok(view.render(ontology.features));
    	    
    	  } else {
    	    flash("error", "Missing file");
    	    return ok(index.render("File Not Found"));	
    	  }
    }
    
    /**
     * Handles the ontology query
     * @throws OWLOntologyCreationException 
     */
   // public static Result getResults(List<String> selected){
    public static Result getResults() throws OWLOntologyCreationException{

    	
    	//DynamicForm requestData = form().bindFromRequest();
    	//System.out.println(request().body().asFormUrlEncoded().get("selected"));
    	String[] selected = request().body().asFormUrlEncoded().get("cars");
    	ArrayList<String> selectedItems = new ArrayList<String>();
    	selectedItems.clear();
    	
    	for(int i=0; i< selected.length; i++){
    		selectedItems.add(selected[i]);
    	}
    	
    	//System.out.println((selectedItems));
    	//System.out.println(List1Comments.size()); // list with ALL comments
    	//System.out.println(CharacterArraylist.size());// list key characters
    	
    	//System.out.println(KeyDescriptionArraylist.size());//list with COMMENTS which are actual comments and not blank comments
    	
    	
    	displayBees(selectedItems);
    	
    	//ArrayList<String>
    	//String[] selected = params.getAll("selected");
    	///return ok("Results:"+ beeModel);
    	
    	return ok(results.render(selectedItems,beeModel));
    	
    }
    
   
    
    private static void loadOntology(String Path) throws OWLOntologyCreationException  {
    	
    	//System.load("/Users/danielfuterman/dll/libFaCTPlusPlusJNI.jnilib");
    	//Load the Ontology and declare all ontology variables
		 manager = OWLManager.createOWLOntologyManager();
		 BeeOntologyfile = new File(Path);
		 //Xylocopa_v4
		 //BeeKeyOntology_V05
		 //BeeKeyOntology_v07
		 // AuronaMac:Applications/BeeKeyOntology_v07.owl
		 //C:/Windows/System32/BeeKeyOntology_v07.owl
		 if (BeeOntologyfile.exists())
			 
		 {
			 	
		 BeeCharacteristics = manager.loadOntologyFromOntologyDocument(BeeOntologyfile);
		 factory1 = manager.getOWLDataFactory();
		 
		 //Print what and where from out
		 System.out.println("Loaded ontology: " + BeeCharacteristics);
	     IRI documentIRI = manager.getOntologyDocumentIRI(BeeCharacteristics);
	     System.out.println("    from: " + documentIRI);

	    //Renderer to get the string text
	    renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	    
	     fppRF = new FaCTPlusPlusReasonerFactory();
	     progressMonitor = new TimedConsoleProgressMonitor();
	     config = new SimpleConfiguration(progressMonitor);
	     config.getProgressMonitor();
	        // Create a reasoner that will reason over our ontology and its imports
	        // closure. Pass in the configuration.
	        //OWLReasoner reasoner = Rf.createReasoner(BeeCharacteristics, config);
	     	// System.out.println("before reasoner create");
	     reasoner = fppRF.createReasoner(BeeCharacteristics, config);
		 }
    }

    
    
    
    public static void loadBeeCharacteristics() throws InterruptedException, OWLOntologyCreationException{
		//Gets the Key characteristics from the ontology within another method,
		//LoadKeyCharacteristics() then places them into an arraylist and then into the listmodel
		//to display on the interface
			CharacterArraylist.clear();
			CharacterArraylist = LoadKeyCharacteristics();
			//ArrayList<String> KeyDescriptionArraylist = new ArrayList<String>();
			//pBar.close();
			List1Comments.clear();
		  
		    KeyDescriptionArraylist.clear();
			//CharacterArraylist.add("char1"); 
			// Here we take each element (each KeyCharacteristic) in the characterarraylist
			// and get the associated comment and then the subproperty of comment 
			// (keydescription) to be displayed on the UI
		for (String element : CharacterArraylist)
		{
				//model.addElement(element);
				
	
		//get the URI of the class (the KeyCharacteristic)
		 OWLClass keycharr = factory1.getOWLClass(IRI.create(BeeCharacteristics.getOntologyID()
		  .getOntologyIRI().toString() + "#" + element));    
		 
			String comment = new String();			 
		
		//Initialize the annotation(comment) property label
		OWLAnnotationProperty label = factory1
		   .getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());
					
		//(keydescription) property, initialize and create the comment property
		OWLAnnotationProperty commentprop = factory1
	     .getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());
		
		//get the keyDescription annotation property which is a subproperty of the annotation 'comment'
		 Set<OWLAnnotationProperty> keyDescription = commentprop.getSubProperties(BeeCharacteristics);
		//place the sub property 'keyDescription' in the annotation(comment) property label
		 for(OWLAnnotationProperty commentkeydesc : keyDescription)
	 		{
		 		label = commentkeydesc;
			}
		 
		 //get the actual string comments (getAnnotations) under keydescription and add them to the 
		 //character list.
		 Set<OWLAnnotation> comments = keycharr.getAnnotations(BeeCharacteristics, label);
			 	for(OWLAnnotation commentAnnot : comments)
		 		{
			 		comment = renderer.render(commentAnnot.getValue());
			 		//System.out.println(commentAnnot);
				}
			 	
			 	
			 	//////////////////////////////
			 	String S = new String();
		    	S = comment;
		    	String newStr = new String();
		    	if (S.contains("\""))
				{
		    		
					int i = S.indexOf("\"", 2);
					if (Character.isWhitespace(S.charAt(i-1))) {
					//int p = S.indexOf(">");
					S = S.substring(0, i-1);
					S = S.trim();
					S = (S + "\"");
					}
				}
		    	//////////////////////////
			 	
			 	//add ALL of the annotations to one array List1Comments
			 	//even the empty annotations
			 	List1Comments.add(S);
			 	
			 	//Add the not empty comments to another arraylist to sort and to 
			 	//add to the UI
			 if (!(S.isEmpty()))
					 {
				 KeyDescriptionArraylist.add(S);
				//model.addElement(comment);
			 }
		
			}
			
		//sort the descriptions and then add them to the listbox on the UI
		Collections.sort(KeyDescriptionArraylist);
		
		
	/*	//ignore
		for (String keyDesc : KeyDescriptionArraylist)
		{
			//model.addElement(keyDesc); NOT USED
			model.add(keyDesc);
		}
			charCount = (CharacterList.size());
			//characterCount.setText("" + charCount);
		*/	
			//System.out.println(List1Comments);
			//System.out.println(CharacterArraylist);
			
	
			//String s = new String();
			//s = ("\"I key\"");
				//	System.out.println(List1Comments.indexOf(s));
	
		}
    
    public static ArrayList<String> LoadKeyCharacteristics() throws OWLOntologyCreationException{
		//Retrieve the the sub classes of KeyCharacteristic, loop through
		//to find the bottom level of classes to return
				
				ArrayList<String> Characteristics = new ArrayList<String>();
				Characteristics.clear();
				//  OntologyLoad OntLoad = new OntologyLoad();
			      //  OntLoad.LoadOnly();
	        
		//Reference the KeyCharacteristics Class
				 OWLClass KeyChar = factory1.getOWLClass(IRI.create(BeeCharacteristics.getOntologyID()
				    		  .getOntologyIRI().toString() + "#DiagnosticFeature"));
    	
				// System.out.println("    first get subclasses ");
				 
		//Get all the subclasses of KeyCharacteristic and loop through to return the bottom level classes
		//direct subclasses should be retrieved (true) or if all subclasses (descendant) (false)
				 NodeSet<OWLClass> subClasses1 = reasoner.getSubClasses(KeyChar, false);
				 
				    for (Node<OWLClass> cls: subClasses1){
  	 	
				    	String firstSubClass = new String();
				    	firstSubClass = renderer.render(cls.getRepresentativeElement());
				    	
				    	String S = new String();
				    	S = cls.toString();
				    	String newStr = new String();
				    	if (S.contains("#"))
						{
							int i = S.indexOf("#");
							int p = S.indexOf(">");
		
							newStr = S.substring((i+1), p);
						}
				    	
				    	OWLClass KeyCharSub1 = factory1.getOWLClass(IRI.create(BeeCharacteristics.getOntologyID()
					    		  .getOntologyIRI().toString() + "#" + firstSubClass));
    		    	
				    	for (Node<OWLClass> subcls: reasoner.getSubClasses(KeyCharSub1, true)){
	    		
				    		if (subcls.isBottomNode() && (!(firstSubClass.contains("Nothing")))){
			    		
				    			Characteristics.add(newStr);		    		
				    		}
				    			else{
		    			
				    			}
				    	}
				    		
				    }
	
				  Collections.sort(Characteristics);
				    return Characteristics;
    }
    
    public static void displayBees(ArrayList<String> selected) throws OWLOntologyCreationException{

		//displayBees(): Gets the user selected info (getUserinfo()) then uses that to 
		//query the ontology and find the bees which match the characteristics (findBees(CharacterArraylistSelected))
		
		//if nothing selected display message to choose at least one
			/*int x = CharacterList.getSelectedValues().length;
			if ( x == 0)
			{
				JDialog NoneselectedDialog = new JDialog(this, "No Diagnostic Features Selected", true);
				NoneselectedDialog.add(new Label("     Please select at least one Diagnostic "
						+ "Feature    "));
				
				NoneselectedDialog.addWindowListener(this);
				NoneselectedDialog.pack();
				//d2.setSize(30, 15);
				NoneselectedDialog.setLocation(550, 200);
				NoneselectedDialog.setVisible(true);
				//System.out.println("Choose!");
			}
			else
			{
			*/
		//Display progress bar while searching for specific bees
		//	loadingBeesDialog();
			
			ArrayList<String> CharacterArraylistSelected = new ArrayList<String>();	
			ArrayList<String> CharacterArraylistSelected2 = new ArrayList<String>();
			CharacterArraylistSelected.clear();
			CharacterArraylistSelected2.clear();
			
		/* getUserinfo: Takes all user input selected characteristics and stores in an arraylist
		* returns the array of characteristics*/
			CharacterArraylistSelected = selected;
			String s = new String();
			int i = 0;
			//for each selected keydescription, find the index of where in the arraylist: List1Comments
			//it is and place into i.
			//Then find the corresponding KeyCharacteristic in the arraylist: CharacterArraylist
			//with the same index and add that KeyCharacteristic to the arraylist: CharacterArraylistSelected2
			
			
			/**
			 * TODO
			 */
			
			for (String Keydescription : CharacterArraylistSelected)
			{
				//s = ( "\""+Keydescription+"\"");
				i = List1Comments.indexOf(Keydescription);
				s = CharacterArraylist.get(i);
				CharacterArraylistSelected2.add(s);
			}
			
			//System.out.println(i);
			//System.out.println(s);
						
			//BeeSpecies b = new BeeSpecies();
			ArrayList<String> BeesArraylist = new ArrayList<String>();
			BeesArraylist.clear();
		//findBees(ArrayList<String> info): @param is the arraylist of user selected characteristics
		//which are used to query the set of bees which have a hasKeyCharacteristic relationship with 
		//all of them		
			
			
			/**
			 * TODO
			 */
			//BeesArraylist = findBees(CharacterArraylistSelected2);
			BeesArraylist = findBees(CharacterArraylistSelected2);
			
			
			
			
			
			//BeesArraylist = b.findBees(CharacterArraylistSelected);
			
			beeModel.clear();
		
		//add found bees to beemodel list on the UI
			for (String selectedItem : BeesArraylist)
			{
				beeModel.add(selectedItem);
				//addElement(selectedItem);
				
			}

		//above will be getbees	//displaybees(bees)
			//below will be display bees method called here
			//BeesList.add(bees);
			//loadingBeesDialog.setVisible(false);
		//}

			//beeCountnumber = (BeesList.getModel().getSize());
			//beeCount.setText("" + beeCountnumber);
			
			
			//this.setEnabled(true);
			
			//takes the array of bees and outputs it to a label etc on the frame
		}
    
    public static ArrayList<String> findBees(ArrayList<String> info) throws OWLOntologyCreationException{
    	
		//findBees(ArrayList<String> info): @param is the arraylist of user selected characteristics
		//which are used to query the set of bees which have a hasKeyCharacteristic relationship with 
		//all of them
		
		//Instance of the hasKeyCharacteristic object property
	     OWLObjectProperty hasKeyChar = factory1.getOWLObjectProperty((IRI.create(BeeCharacteristics.getOntologyID()
	   		  .getOntologyIRI().toString() + "#hasDiagnosticFeature")));
 
	    ArrayList<OWLClass> Arraylist1 = new ArrayList<OWLClass>();
			//String bees = (info + "   are bee characteristics");
	    
	   //loop through the user selected info and create their IRIs and add to an arraylist
			for (String Item : info)
			{
				OWLClass Char = factory1.getOWLClass(IRI.create(BeeCharacteristics.getOntologyID()
			      		  .getOntologyIRI().toString() + "#" + Item));
				
					Arraylist1.add(Char);
			//	BeesArraylist1.add(Item + " Bee 1");
			}
			
			    Set<OWLClassExpression> userkeys = new HashSet<OWLClassExpression>();
		//loop throught the arraylist of user selected IRIs and add them all to a hashset userkeys	    
		//along with the hasKeychar object property
			    for (OWLClass userInfo : Arraylist1)
			    {
			
			    	userkeys.add(factory1.getOWLObjectSomeValuesFrom(hasKeyChar,
					 userInfo));
	  			
			    }
			
		 ArrayList<String> BeesArraylistFinal = new ArrayList<String>();
		 
		 //Instantiate an intersection of all the classes with all the selected characteristics
		//and the object property hasKeyChar
		  OWLObjectIntersectionOf intersection = factory1.getOWLObjectIntersectionOf(userkeys);
		 
		 // System.out.println(" just before get subclasses of fginding bees");
		
		//get all subclasses of the intersection
		  NodeSet<OWLClass> foundBees2 = reasoner.getSubClasses(intersection, false);
  
		//loop through the found bees and add each bee to the arraylist BeesArraylistFinal
		// and then return BeesArraylistFinal
		  for (Node<OWLClass> bee2 : foundBees2)
			{
				 
				 if (!(bee2.isBottomNode()))
				 {
				// System.out.println("hope  " + renderer.render(bee2.getRepresentativeElement()));
				// BeesArraylistFinal.add(OntLoad.renderer.render(bee2.getRepresentativeElement()));
				
				 OWLClass beeFound = factory1.getOWLClass
						 (IRI.create(BeeCharacteristics.getOntologyID()
			    		  .getOntologyIRI().toString() + "#" + 
			    		  renderer.render(bee2.getRepresentativeElement())));
				 	
				 String S = new String();
				 S = bee2.toString();
				 String newBeeStr = new String();
			    	if (S.contains("#"))
					{
						int i = S.indexOf("#");
						int p = S.indexOf(">");
						
						
						newBeeStr = S.substring((i+1), p);
					}
			    	
					// System.out.println(beeFound);
				 BeesArraylistFinal.add(newBeeStr);
		
				 }
				
			}
		
			 if (BeesArraylistFinal.isEmpty())
			 {
			 BeesArraylistFinal.add("There are no bees that match "
	 		 		+ "your Diagnostic Features Selection");
			 }
			 
			 Collections.sort(BeesArraylistFinal);
			 return BeesArraylistFinal;

	}
}
