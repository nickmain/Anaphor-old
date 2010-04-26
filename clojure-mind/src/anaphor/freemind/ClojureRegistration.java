package anaphor.freemind;

import java.util.logging.Logger;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import clojure.lang.DynamicClassLoader;
import clojure.lang.RT;
import freemind.extensions.HookRegistration;
import freemind.modes.MindMap;
import freemind.modes.MindMapNode;
import freemind.modes.ModeController;
import freemind.modes.mindmapmode.MindMapController;

public class ClojureRegistration implements HookRegistration {

    static final Logger logger = Logger.getLogger( "Clojure" );
    private static ClassLoader loader = new DynamicClassLoader( ClojureRegistration.class.getClassLoader() );
    
    //load the clojure code
    static {
        try {         
            logger.info( "Loading Clojure boot script.." );
            Thread.currentThread().setContextClassLoader( loader );
            clojure.main.main( new String[] { "@/anaphor/freemind/clojuremind.clj" } );
        }
        catch( Exception e ) {
            e.printStackTrace();            
        }        
    }
    
    private final MindMapController controller;
    private TreeModelListener listener;

    
    
    public ClojureRegistration( ModeController controller, MindMap map ) {
        this.controller = (MindMapController) controller;
    }

    /**
     * @see freemind.extensions.HookRegistration#register()
     */
    public void register() {
        logger.info( "Clojure register" );

        //add a listener to the mindmap model
        listener = new TreeModelListener() {
            @Override public void treeNodesChanged( TreeModelEvent e ) {
                ClojureRegistration.logger.info( "**** treeNodesChanged ****" );
                
                Object[] path = e.getPath();
                MindMapNode node = (MindMapNode) path[path.length - 1];
                
                Thread.currentThread().setContextClassLoader( loader );
                try {
                    RT.var( "anaphor.freemind.clojuremind", "on-node-change" )
                      .invoke( controller, node );
                }
                catch( Exception ex ) {
                    ClojureRegistration.logger.warning( "Clojure Exception: " + ex.getMessage() );
                }            
            }
            @Override public void treeNodesInserted( TreeModelEvent e ) {
                //TODO
            }
            @Override public void treeNodesRemoved( TreeModelEvent e ) {
                //TODO
            }
            @Override public void treeStructureChanged( TreeModelEvent e ) {
                //TODO
            }
        };
        
        controller.getMap().addTreeModelListener( listener );
    }

    /**
     * @see freemind.extensions.HookRegistration#deRegister()
     */
    public void deRegister() {
        logger.info( "Clojure deregister" );
        
        if( listener != null ) {
            controller.getMap().removeTreeModelListener( listener );
            listener = null;
        }
    }
}