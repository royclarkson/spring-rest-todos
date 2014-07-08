var fab = require('fabulous');
var rest = require('fabulous/rest');
var Sync = require('fabulous/data/Sync');
var Observer = require('fabulous/data/Observer');
var PatchClient = require('fabulous/data/PatchClient');
//var DSClient = require('fabulous/data/DSClient');
//var RestClient = require('fabulous/data/RestClient');

var TodosController = require('./TodosController');

exports.main = fab.run(document.body, todosApp);

function todosApp(node, context) {
	context.controller = new TodosController([]);

	new Sync([
		new PatchClient(rest.at('/todos')),
		Observer.fromProperty('todos', context.controller)
	], 1000).run(context.scheduler);
}
