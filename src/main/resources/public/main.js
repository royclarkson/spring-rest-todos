var fab = require('fabulous');
var rest = require('fabulous/rest');
var Document = require('fabulous/Document');

var TodosController = require('./TodosController');

exports.main = fab.run(document.body, todosApp);

function todosApp(node, context) {
	context.controller = new TodosController([]);

	var getClient = rest.at('/todos');
	var patchClient = rest.at('/sync/todos');

	Document.sync([
		Document.fromPatchRemote(function(patch) {
			return patchClient.patch({ entity: patch });
		}, getClient.get()),
		Document.fromProperty('todos', context.controller)
	]);
}

