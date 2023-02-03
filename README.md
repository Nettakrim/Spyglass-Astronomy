# Spyglass Astronomy
Explore a procedurally generated solar system through the lens of a spyglass!

The mod is entirely client-side and designed for helping with worldbuilding and immersion, it is encouraged you think of lore about all the constellations you draw and planets you name!

Planets and stars will be the same for all players on a particular world, and are generated randomly (and largely scientifically accurately) per world, and you can easily share constellations between players using a client-side command

![Planet overlooking a far away mountain](https://cdn.modrinth.com/data/EdBSdqge/images/3dc17c154655fdc2dc7fc3989a38e16e9052d269.png)
# How to use

To interact with the cosmos there are three spyglass modes: vanilla, constellation (amethyst), and star (emerald), these are cycled with the pick block button (middle click) while zoomed in.

Constellation mode allows you to draw constellations by holding the attack button (left click) and drawing between stars, as well as being able to select constellations

Star mode allows you to select stars and planets, note that comets are referred to as *planets* when using commands

Once an object is selected you can name it with `/sga:name <name>`, or get useful information with `/sga:info`, you can also get the information of a specific object without selecting it by following the suggestions after typing `/sga:info <x>`

If youre playing on a multiplayer server, you can use `/sga:share` or `/sga:share <x>` to share the currently selected or specified object respectively, this gives you a code you can send in chat or /message to someone directly, which allows them to easily add a constellation or name of a planet or star on their side, provided they have the mod too, of course.

All the client side commands can be seen by looking at the suggestions after typing `/sga:`, or by refering to the list below

# Commands

### `/sga:info`

most information is not given until you fufill a certain criteria, this can be bypassed with `/sga:admin bypassknowledge`

`/sga:info`

redirects to the appropriate command for the currently selected constellation, star or planet

`/sga:info constellation <name>`

gets information about the constellation with name `<name>`
- Name
- Current position in the sky (the two numbers shown on F3)
- What time of year it is most visible

`/sga:info star <name>`

gets information about the star with name `<name>`
- Name
- Current position in the sky (the two numbers shown on F3)
- What time of year it is most visible

`/sga:info planet <name>`

gets information about the planet with name `<name>`
- Name
- Orbital Period
- Orbital Resonance (amount of days between each closest aproach of thisworld and the specified planet, assuming circular orbits)
- Current position in orbit (% of the way through the planets year)
- Distance from thiswoorld (in AU)
- Eccentricity
- Inclination

`/sga:info thisworld`

gets information about the minecraft world's orbit
- Name
- Orbital Period
- Current position in orbit (% of the way through the planets year)
- Eccentricity
- Inclination

### `/sga:select`

`/sga:select constellation|star|planet <name>`

selects the specified object with name `<name>`

### `/sga:name`

`/sga:name <name>`

names the currently selected object `<name>`, quotations are not needed (its like the `/say` command)

### `/sga:share`

`/sga:share`

redirects to the appropriate command for the currently selected constellation, star or planet

`/sga:share constellation|star|planet <name>`

displays a [Click Here] button, clicking the button will open chat with some suggested text representing the object, if anyone recieves this text as part of a message (e.g, though just sending it in chat directly, or with a /msg), they get a [Click Here] button which when clicked gives them the shared object

### `/sga:admin`

there is generally little reason to use `sga:admin` commands, but some may still be useful for messing around

`/sga:admin remove <name>`

removes the constellation with name `<name>`

`/sga:admin setstarcount <amount>`

changes the amount of stars in the sky to `<amount>`, default 1024, max 4095

`/sga:admin discardchanges`

reverts to the last saved data, data is saved whenever you leave the world or change dimension

`/sga:admin bypassknowledge`

bypass knowledge checks for `/sga:info` until a relog

`/sga:admin setyearlength <days>`

sets the length of the year in days, decimals are allowed but it has to be 0.125 or more, by default it is 8

`/sga:admin setseed star <seed>`

sets the seed that stars use to generate, by default this is the biome seed of the world

`/sga:admin setseed planet <seed>`

sets the seed that planets use to generate, by default this is the biome seed of the world

`/sga:admin rename constellation|star|planet <index> <name>`

renames the `<index>`th object of the specified type to `<name>`, then selects it
there is little reason you would want to use this, however it is needed for `/sga:share` to work

### `/sga:hide`

`/sga:hide`

toggles visibilities of stars, constellations, and planets

`/sga:hide stars|constellations|planets`

toggles visiblity of the respective type of object

`/sga:hide vanillastars`

toggles visiblity of the boring, vanilla stars, i dont know why you would want to do this