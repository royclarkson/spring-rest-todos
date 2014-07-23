var fab = require('fabulous');
var rest = require('fabulous/rest');
var Document = require('fabulous/Document');

var TodosController = require('./TodosController');

exports.main = fab.run(document.body, todosApp);

function todosApp(node, context) {
	context.controller = new TodosController([]);

	var client = rest.at('/todos');

	Document.sync([
		Document.fromPatchRemote(function(patch) {
			return client.patch({ entity: patch });
		}, client.get()),
		Document.fromProperty('todos', context.controller)
	]);
}

