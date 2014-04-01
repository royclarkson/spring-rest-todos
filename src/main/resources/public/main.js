var fluent = require('wire/config/fluent');
var JsonPatch = require('cola/data/JsonPatch');
var TodosController = require('./TodosController');

module.exports = fluent(function(context) {
	return context
		.add('todos@controller', TodosController)
		.add('todos@model', function() {
			return new JsonPatch('/todos');
		});
});