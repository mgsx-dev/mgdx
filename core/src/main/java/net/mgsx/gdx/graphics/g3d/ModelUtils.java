package net.mgsx.gdx.graphics.g3d;

import java.util.function.Consumer;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.utils.Array;

public class ModelUtils {

	public static void eachNodeRecusrsive(Iterable<Node> nodes, Consumer<Node> callback){
		for(Node node : nodes){
			callback.accept(node);
			if(node.hasChildren()) eachNodeRecusrsive(node.getChildren(), callback);
		}
	}
	
	public static Array<NodePart> collectNodeParts(ModelInstance modelInstance) {
		Array<NodePart> results = new Array<NodePart>();
		eachNodeRecusrsive(modelInstance.nodes, node->results.addAll(node.parts));
		return results;
	}

}
