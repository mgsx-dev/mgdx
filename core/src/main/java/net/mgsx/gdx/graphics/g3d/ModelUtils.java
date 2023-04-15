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
	
	public static void eachNodeRecusrsive(Node node, Consumer<Node> callback){
		callback.accept(node);
		if(node.hasChildren()) eachNodeRecusrsive(node.getChildren(), callback);
	}
	
	public static Array<NodePart> collectNodeParts(ModelInstance modelInstance) {
		Array<NodePart> results = new Array<NodePart>();
		eachNodeRecusrsive(modelInstance.nodes, node->results.addAll(node.parts));
		return results;
	}
	public static Array<Node> collectNodes(ModelInstance modelInstance) {
		Array<Node> results = new Array<Node>();
		eachNodeRecusrsive(modelInstance.nodes, node->results.add(node));
		return results;
	}

	public static void eachNodePartRecusrsive(Iterable<Node> nodes, Consumer<NodePart> callback) {
		eachNodeRecusrsive(nodes, node->{
			for(NodePart part : node.parts){
				callback.accept(part);
			}
		});
	}
	public static void eachNodePartRecusrsive(Node node, Consumer<NodePart> callback) {
		eachNodeRecusrsive(node, n->{
			for(NodePart part : n.parts){
				callback.accept(part);
			}
		});
	}

}
